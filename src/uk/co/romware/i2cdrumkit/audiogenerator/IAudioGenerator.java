/**********************************************************************************************************/
/*                                                                                                        */
/* This file contains an interface that represents a class that will accept a MIDI note and play a sound  */
/*                                                                                                        */
/* Use at your own risk                                                                                   */
/*                                                                                                        */
/**********************************************************************************************************/
package uk.co.romware.i2cdrumkit.audiogenerator;

public interface IAudioGenerator {
	public void playNote (int p_Note, int p_Velocity);
	public void stopNote (int p_Note);

}
