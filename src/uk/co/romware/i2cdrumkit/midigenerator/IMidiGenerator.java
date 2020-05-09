/**********************************************************************************************************/
/*                                                                                                        */
/* This file contains an interface that represents a class that generates MIDI "note on" events           */
/*                                                                                                        */
/* Use at your own risk                                                                                   */
/*                                                                                                        */
/**********************************************************************************************************/
package uk.co.romware.i2cdrumkit.midigenerator;

public interface IMidiGenerator {
	
	public interface IMidiGeneratorListener {
		public void noteOn(int p_Note, int p_Velocity);
	}

	public void addListener(IMidiGeneratorListener p_Listener);
	public boolean start();
}
