/**********************************************************************************************************/
/*                                                                                                        */
/* I2CDrumKit appliation entry point                                                                      */
/*                                                                                                        */
/* This file contains the start point for the application to read drum signals from the I2C interface     */
/* and generate appropriate sounds.                                                                       */
/*                                                                                                        */
/* Use at your own risk                                                                                   */
/*                                                                                                        */
/**********************************************************************************************************/
package uk.co.romware.i2cdrumkit;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.romware.i2cdrumkit.audiogenerator.IAudioGenerator;
import uk.co.romware.i2cdrumkit.audiogenerator.sampled.SampledAudioGenerator;
import uk.co.romware.i2cdrumkit.midigenerator.IMidiGenerator;
import uk.co.romware.i2cdrumkit.midigenerator.IMidiGenerator.IMidiGeneratorListener;
import uk.co.romware.i2cdrumkit.midigenerator.i2c.I2CHandler;



public class I2CDrumKit {

	private final static Logger LOGGER = LoggerFactory.getLogger(I2CDrumKit.class);

	public static void main(String[] args) throws Exception {
		LOGGER.info("Starting");

		//IAudioGenerator gen = new MidiAudioGenerator();
		//IAudioGenerator gen = new ServerAudioGenerator();
		
		// This takes MIDI noteOn commands and generates audio  
		IAudioGenerator audioGen = new SampledAudioGenerator();
		
		
		// This gets WII Drum signals via I2C and generates MIDI "note on" commands
		IMidiGenerator midiGen = I2CHandler.getInstance();
		midiGen.addListener(new IMidiGeneratorListener() {
			@Override
			public void noteOn(int p_Note, int p_Velocity) {
				audioGen.playNote(p_Note, p_Velocity);
			}			
		});
		midiGen.start();

		
		LOGGER.info("Started");
	}
		

}
