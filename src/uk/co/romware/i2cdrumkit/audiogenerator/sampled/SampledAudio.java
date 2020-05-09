/**********************************************************************************************************/
/*                                                                                                        */
/* This file contains a class that plays a particular note, and will only play that note one at a time,   */
/* so when the note is requested while the previously requested note is still playing, the first note is  */
/* stopped and the new note played.                                                                       */
/*                                                                                                        */
/* Use at your own risk                                                                                   */
/*                                                                                                        */
/**********************************************************************************************************/
package uk.co.romware.i2cdrumkit.audiogenerator.sampled;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.romware.i2cdrumkit.audiogenerator.sampled.DataLineHandlerFactory.IDataLineHandler;

public class SampledAudio {

	private final static Logger LOGGER = LoggerFactory.getLogger(SampledAudio.class);

	private byte [] m_Data;
	private AudioFormat m_Format;
	private IDataLineHandler m_CurrentHandler;
	private Object m_Lock = new Object();
	private DataLineHandlerFactory m_Factory;
	
	public SampledAudio (String p_FileName, DataLineHandlerFactory p_Factory) throws UnsupportedAudioFileException, IOException {
		m_Factory = p_Factory;
		
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(SampledAudio.class.getResourceAsStream("/" + p_FileName)));
		m_Format = audioStream.getFormat();

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte [] buf = new byte [5000];
		int sz = audioStream.read(buf);
		while (sz!=-1) {
			os.write(buf, 0, sz);
			sz = audioStream.read(buf);
		}
		m_Data = os.toByteArray();
		os.close();
		audioStream.close();
	}
	
	public AudioFormat getFormat() {
		return m_Format;
	}
	
	public void play(int p_Volume) {
		synchronized (m_Lock) {
			LOGGER.info("Play requested");
			if (m_CurrentHandler!=null) {
				LOGGER.info("Already playing.  Requesting stop");
				m_CurrentHandler.stop();
			}
			m_CurrentHandler = m_Factory.getHandler();
			m_CurrentHandler.play(m_Data, p_Volume);
			LOGGER.info("Requested play");
		}
	}
	
	
	public void stop() {
		synchronized (m_Lock) {
			LOGGER.info("Stop requested");
			if (m_CurrentHandler!=null) {
				m_CurrentHandler.stop();
			}
			LOGGER.info("Stopped");
		}
		
	}
	
	
}
