/**********************************************************************************************************/
/*                                                                                                        */
/* This file contains a class that converts MIDI "note on" commands to audio using the Java MIDI          */
/* implementation                                                                                         */
/*                                                                                                        */
/* There were a couple of issues with this on the Raspberry PI.  First, neither the Oracle JRE or         */
/* OpenJDK came with any sound banks, which meant that as soon as the MIDI system started, it tried to    */
/* create an emergency sound bank.  I left it running for a few minutes before giving up.                 */
/* I then downloaded the sound banks from Oracle, but they did not sound very good.  However, I found a   */
/* free sound bank at http://ntonyx.com/soft/32MbGMStereo.sf2, which I copied to the jre/lib/audio        */
/* directory on the java implementation.  This produced a good quality sound, but there was a significant */
/* lag between requesting the sound and it being output.                                                  */
/*                                                                                                        */
/* Use at your own risk                                                                                   */
/*                                                                                                        */
/**********************************************************************************************************/
package uk.co.romware.i2cdrumkit.audiogenerator.midi;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.romware.i2cdrumkit.audiogenerator.IAudioGenerator;

public class MidiAudioGenerator implements IAudioGenerator {

	private final static Logger LOGGER = LoggerFactory.getLogger(MidiAudioGenerator.class);

	private MidiChannel m_Channel;
	
	public MidiAudioGenerator() throws MidiUnavailableException {
		LOGGER.info("Setting up MIDI");
		Synthesizer synth = null;
		for (MidiDevice.Info info:  MidiSystem.getMidiDeviceInfo()) {
			MidiDevice dev = MidiSystem.getMidiDevice(info);
			if (dev instanceof Synthesizer) {
				synth = (Synthesizer)dev;
				break;
			}
		}
		
		synth.open();

		MidiChannel[] channels = synth.getChannels();
		
		m_Channel = channels[9]; // Channel 10 is for percussion 
		m_Channel.controlChange(7, 127); // Set volume to max
		
		LOGGER.info("MIDI set up");		
	}
	
	public void playNote (int p_Note, int p_Velocity) {
		m_Channel.noteOn(p_Note, p_Velocity);
	}
	
	public void stopNote (int p_Note) {
		m_Channel.noteOff(p_Note);
	}
	
}

