/**********************************************************************************************************/
/*                                                                                                        */
/* This file contains a class that actually plays the audio data.  It works by sending audio data to a    */
/* source data line, but only giving a few milliseconds worth of data at a time, so that if the audio     */
/* needs to stop, it will happen almost immediately                                                       */
/*                                                                                                        */
/* Use at your own risk                                                                                   */
/*                                                                                                        */
/**********************************************************************************************************/
package uk.co.romware.i2cdrumkit.audiogenerator.sampled;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataLineHandlerFactory {

	private final static Logger LOGGER = LoggerFactory.getLogger(DataLineHandlerFactory.class);
	private final static int MILLISECONDS_TO_BUFFER = 100;

	public interface IDataLineHandler {

		public void play(byte [] p_Data, int p_Volume);
		void stop();
		
	}

	/******************************************************************************************************/
	/*                                                                                                    */
	/* This is the class that does the actual audio output.  It works by creating a thread that waits     */
	/* for a request to play audio and then sends chunks of that data to the SourceDataLine, until        */
	/* either there is no more data to be sent or it is told to stop playing                              */
	/*                                                                                                    */
	/******************************************************************************************************/	
	private class DataLineHandler implements IDataLineHandler {
		
		private SourceDataLine m_Line;
		private byte [] m_CurrentAudio;
		private int m_CurrentPosition;
		private Object m_Lock = new Object();
		private int m_Index;
	
		public DataLineHandler(int p_Index) throws LineUnavailableException {
			m_Index = p_Index;
			
			m_Line = AudioSystem.getSourceDataLine(m_Format);
			m_Line.open();
	
			Thread t = new Thread(new Runnable() {
				public void run() {
					while (true) {
						synchronized (m_Lock) {
							try {
								LOGGER.info("Waiting for audio");
								m_Lock.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if (m_CurrentAudio==null) {
								continue;
							}
							LOGGER.info("Got audio");
							m_CurrentPosition = 0;
							int written = m_Line.write(m_CurrentAudio, 0, m_BytesToBuffer * 2);
							m_CurrentPosition = written;
							m_Line.start();
							while (m_CurrentPosition < m_CurrentAudio.length) {
								
								try {
									m_Lock.wait(10);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								
								if (m_CurrentAudio==null) {
									LOGGER.info("Stopping sending");
									break;
								}
								
								int pending = m_Line.getBufferSize() - m_Line.available();
								if (pending<m_BytesToBuffer*2) {
									int toWrite = (int)Math.min(m_BytesToBuffer, m_CurrentAudio.length-m_CurrentPosition);
									try {
										written = m_Line.write(m_CurrentAudio, m_CurrentPosition, toWrite);
										m_CurrentPosition = m_CurrentPosition + written;
									} catch (Exception ex) {
									}
								}
							}
							m_CurrentAudio = null;
							LOGGER.info("Finished audio");
						}
						m_Line.flush();
						m_AvailableHandlers.add(DataLineHandler.this);
						LOGGER.info("Returned DataLineHandler");
					}
				}
			}, "Data Line Filler - " + p_Index);
			t.start();
		}
		
		public void play(byte [] p_Data, int p_Volume) {
			synchronized (m_Lock) {
				LOGGER.info("Handler " + m_Index + ": Play requested");
				if (m_CurrentAudio != null) {
					throw new RuntimeException("Already playing");
				}
				m_CurrentAudio = p_Data;
				BooleanControl mute = (BooleanControl)m_Line.getControl(BooleanControl.Type.MUTE);
				mute.setValue(false);
				

				// This does not seem to work to change the volume on Raspberry PI
			    FloatControl gainControl = (FloatControl) m_Line.getControl(FloatControl.Type.MASTER_GAIN);
			    float volume = (float)p_Volume/127.0f;
			    double dB = Math.log(volume) / (Math.log(10) * 20);
			    gainControl.setValue((float)dB);
			    
			    
				m_Lock.notify();
			}
		}
		
		public void stop() {
			synchronized (m_Lock) {
				LOGGER.info("Handler " + m_Index + ": Stop requested");
				if (m_CurrentAudio == null) {
					return;
				}
				m_CurrentAudio = null;
				BooleanControl mute = (BooleanControl)m_Line.getControl(BooleanControl.Type.MUTE);
				mute.setValue(true);
				m_Lock.notify();
			}
			
		}
	}

	// The Raspberry PI Java audio system only supports up to 7 data lines simultaneously, hence
	// only 7 handlers are created
	private BlockingQueue<DataLineHandler> m_AvailableHandlers = new ArrayBlockingQueue<DataLineHandler>(7);
	private AudioFormat m_Format;
	private int m_BytesToBuffer;
	
	
	public DataLineHandlerFactory (AudioFormat p_Format) throws LineUnavailableException {
		float framesToBuffer = (p_Format.getFrameRate() * MILLISECONDS_TO_BUFFER) / 1000;
		m_BytesToBuffer = (int)Math.ceil(framesToBuffer * p_Format.getFrameSize());

		int idx = 1;
		m_Format = p_Format;
		while(m_AvailableHandlers.remainingCapacity()>0) {
			m_AvailableHandlers.add(new DataLineHandler(idx));
			idx++;
		}
	}
	
	public IDataLineHandler getHandler() {
		try {
			return m_AvailableHandlers.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
