/**********************************************************************************************************/
/*                                                                                                        */
/* This file contains a class that reads data from the Raspberry PI I2C and converts it into MIDI "note   */
/* on" messages                                                                                           */
/*                                                                                                        */
/* Use at your own risk                                                                                   */
/*                                                                                                        */
/**********************************************************************************************************/
package uk.co.romware.i2cdrumkit.midigenerator.i2c;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.wiringpi.I2C;

import uk.co.romware.i2cdrumkit.midigenerator.IMidiGenerator;
import uk.co.romware.i2cdrumkit.midigenerator.i2c.DrumData.DrumOperation;
import uk.co.romware.i2cdrumkit.midigenerator.i2c.DrumData.DrumStrike;

public class I2CHandler implements IMidiGenerator {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(I2CHandler.class);
	private final static Map<DrumData.ControlType, Integer> CONTROL_MAP = new HashMap<DrumData.ControlType, Integer>();

	// This is a map from WII Drum pads to MIDI note numbers
	static {
		CONTROL_MAP.put(DrumData.ControlType.RED, 38);
		CONTROL_MAP.put(DrumData.ControlType.ORANGE, 49);
		CONTROL_MAP.put(DrumData.ControlType.YELLOW, 42);
		CONTROL_MAP.put(DrumData.ControlType.BLUE, 50);
		CONTROL_MAP.put(DrumData.ControlType.GREEN, 45);
		CONTROL_MAP.put(DrumData.ControlType.PEDAL, 35);
	}


	private static final I2CHandler INSTANCE = new I2CHandler();
	public static final I2CHandler getInstance() {
		return INSTANCE;
	}
	
	
	
	private int m_Fd;
	private Vector<IMidiGenerator.IMidiGeneratorListener> m_Listeners = new Vector<IMidiGenerator.IMidiGeneratorListener>();
	
	public I2CHandler() {
		LOGGER.info("Setting up I2C");
		m_Fd = I2C.wiringPiI2CSetup(0x52);
		LOGGER.info("Set up I2C");
	}
	
	
	public void addListener(IMidiGenerator.IMidiGeneratorListener p_Listener) {
		m_Listeners.add(p_Listener);
	}
	
	private void informListeners(DrumOperation p_Operation) {
		if (p_Operation instanceof DrumStrike) {
			DrumStrike strike = (DrumStrike)p_Operation;

			int note = CONTROL_MAP.get(strike.getDrumPad());
			int velocity = (int)((6-strike.getSoftness()) * (127.0/6.0));
					
					
			for (IMidiGenerator.IMidiGeneratorListener listener : m_Listeners) {
				listener.noteOn(note, velocity);
			}
		}
	}
	
	private boolean restart() {
		
		int ret = I2C.wiringPiI2CWriteReg8( m_Fd, 0xf0, 0x55);
		if (ret!=0) {
			LOGGER.error("Failed to write");
			return false;
		}
		try {
			Thread.sleep(1);
		} catch (Exception ex) {
		}
		ret = I2C.wiringPiI2CWriteReg8( m_Fd, 0xfb, 0x00);
		if (ret!=0) {
			LOGGER.error("Failed to write");
			return false;
		}
		try {
			Thread.sleep(1);
		} catch (Exception ex) {
		}
		
		return true;
	}
	
	
	public boolean start() {
		LOGGER.info("Starting I2C");

		if (!restart()) {
			return false;
		}
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				DrumOperation lastOperation = null;
				long notBefore = Long.MIN_VALUE;
				
				while (true) {
					if (I2C.wiringPiI2CWrite(m_Fd, 0x00)!=0) {
						LOGGER.error("Failed to write - attempting to restart");
						while (!restart()) {
							try {
								Thread.sleep(1000);
							} catch (Exception ex) {
							}
						}
						continue;
					}

					DrumOperation op = DrumData.getDrumOperation(
							(byte)I2C.wiringPiI2CRead(m_Fd),
							(byte)I2C.wiringPiI2CRead(m_Fd),
							(byte)I2C.wiringPiI2CRead(m_Fd),
							(byte)I2C.wiringPiI2CRead(m_Fd),
							(byte)I2C.wiringPiI2CRead(m_Fd),
							(byte)I2C.wiringPiI2CRead(m_Fd));
					if (op!=null) {
						/**********************************************************************************/
						/*                                                                                */
						/* For some reason, I was getting the same note sent multiple times, so I wrote   */
						/* this to ignore duplicate notes within 10 milliseconds of each other            */
						/*                                                                                */
						/**********************************************************************************/
						long curTime = System.currentTimeMillis();
						if (notBefore<curTime) {
							LOGGER.info(op.toString());
							lastOperation = op;
							notBefore = curTime + 10;
							informListeners(op);
						} else {
							if (!op.equals(lastOperation)) {
								LOGGER.info(op.toString());
								lastOperation = op;
								notBefore = curTime + 10;
								informListeners(op);
							} else {
								LOGGER.info("Skipping duplicate");
							}
						}
					}
				}
			}
		});
		t.setName("I2C Monitor");
		t.start();
		return true;
	}
	
}
