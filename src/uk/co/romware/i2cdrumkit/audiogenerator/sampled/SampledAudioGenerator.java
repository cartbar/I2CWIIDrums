/**********************************************************************************************************/
/*                                                                                                        */
/* This file contains a class that takes MIDI "note on" commands and generates audio by playing audio     */
/* files.                                                                                                 */
/* My initial approach was to use Clip objects, however, there appears to be a bug in the Raspberry PI    */
/* implementation as the Clip.stop() method does not stop the clip from playing and trying to mute the    */
/* clip didn't work either.  Instead, I created a set of classes the feed audio data to the Java audio    */
/* engine in chunks, small enough that when I want to stop the audio, it stops feeding data and the       */
/* engine only has a few milliseconds of data in its buffer                                               */
/* The audio files came from https://www.musicradar.com/, although I had to convert them to 16 bit        */
/* because Java didn't support the original format.                                                       */
/*                                                                                                        */
/* Use at your own risk                                                                                   */
/*                                                                                                        */
/**********************************************************************************************************/
package uk.co.romware.i2cdrumkit.audiogenerator.sampled;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.romware.i2cdrumkit.audiogenerator.IAudioGenerator;


public class SampledAudioGenerator implements IAudioGenerator { 	

	private final static Logger LOGGER = LoggerFactory.getLogger(SampledAudioGenerator.class);

	private Map<Integer, SampledAudio> m_Clips = new HashMap<Integer, SampledAudio>();
	
	public SampledAudioGenerator () throws UnsupportedAudioFileException, IOException, LineUnavailableException  {
		
		AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(SampledAudioGenerator.class.getResourceAsStream("/CyCdh_K3Crash-02-16.wav")));
		AudioFormat fmt = ais.getFormat();
		ais.close();
		DataLineHandlerFactory factory = new DataLineHandlerFactory(fmt);		
		
		LOGGER.info("Loading clips");
		
		// This maps MIDI note numbers to particular audio files
		m_Clips.put(42,  new SampledAudio("CyCdh_K3Crash-02-16.wav", factory));
		m_Clips.put(49,  new SampledAudio("CyCdh_K3HfHat-16.wav", factory));
		m_Clips.put(45,  new SampledAudio("CyCdh_K3Tom-01-16.wav", factory));
		m_Clips.put(50,  new SampledAudio("CyCdh_K3Tom-04-16.wav", factory));
		m_Clips.put(38,  new SampledAudio("CyCdh_K3SdSt-07-16.wav", factory));
		m_Clips.put(35,  new SampledAudio("CyCdh_K3Kick-01-16.wav", factory));

		for (SampledAudio audio : m_Clips.values()) {
			if (!audio.getFormat().matches(fmt)) {
				throw new IOException("Mismatched audio format");
			}
		}
		LOGGER.info("Loaded clips");
		
	}

	public void playNote (int p_Note, int p_Velocity) {
		try {
			m_Clips.get(p_Note).play(p_Velocity);
		} catch (Exception ex) {			
		}
	}
	
	
	public void stopNote (int p_Note) {
		m_Clips.get(p_Note).stop();
	}
}
