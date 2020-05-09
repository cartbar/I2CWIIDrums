/**********************************************************************************************************/
/*                                                                                                        */
/* This file contains a class that converts I2C data into WII Drum commands                               */
/* Note that the detection of Plus and Minus buttons was a bit unreliable, so I removed it                */
/* The information about the format of the I2C data came from                                             */ 
/* https://wiibrew.org/wiki/Wiimote/Extension_Controllers/Guitar_Hero_World_Tour_(Wii)_Drums              */
/*                                                                                                        */
/* Use at your own risk                                                                                   */
/*                                                                                                        */
/**********************************************************************************************************/
package uk.co.romware.i2cdrumkit.midigenerator.i2c;

public class DrumData {

	public abstract static class DrumOperation {		
	}
/*	
	public static class PlusButton extends DrumOperation {
		public String toString() {
			return "Plus Button";
		}
		private PlusButton() {
		}
		
		public boolean equals(Object p_Object) {
			return p_Object instanceof PlusButton;
		}
	}
	private static final PlusButton PLUS_BUTTON = new PlusButton();
	

	public static class MinusButton extends DrumOperation {
		public String toString() {
			return "Minus Button";
		}
		private MinusButton() {			
		}
		public boolean equals(Object p_Object) {
			return p_Object instanceof MinusButton;
		}
	}
	private static final MinusButton MINUS_BUTTON = new MinusButton();
*/
	
	public static class DrumStrike extends DrumOperation {
		private ControlType m_DrumPad;
		private int m_Softness;
		
		public DrumStrike (ControlType p_DrumPad, int p_Softness) {
			m_DrumPad = p_DrumPad;
			m_Softness = p_Softness;
		}
		
		public ControlType getDrumPad() {
			return m_DrumPad;
		}

		public int getSoftness() {
			return m_Softness;
		}
	
		public String toString() {
			return m_DrumPad + " - " + m_Softness;
		}

		public boolean equals(Object p_Object) {
			if (!(p_Object instanceof DrumStrike)) {
				return false;
			}
			DrumStrike obj = (DrumStrike)p_Object;
			
			return (obj.m_Softness == m_Softness) && (obj.m_DrumPad.equals(m_DrumPad));
		}

	}

	
	public static enum ControlType {
		ORANGE,
		RED,
		BLUE,
		YELLOW,
		GREEN,
		PEDAL
	}
	
	private DrumData() {
	}

	static public DrumOperation getDrumOperation(byte p_Data0, byte p_Data1, byte p_Data2, byte p_Data3, byte p_Data4, byte p_Data5) {
/*
        if ((p_Data4 & 16) == 0) {
        	return MINUS_BUTTON;
        }

        if ((p_Data4 & 4) == 0) {
        	return PLUS_BUTTON;
        }
*/
        boolean gotVelocity = (p_Data2 & 64) == 0;
        if (gotVelocity) {
        	int softness = (p_Data3 & 224) >> 5;
            int which = (p_Data2 & 62) >> 1;
            
            switch (which) {
            	case 27: return new DrumStrike(ControlType.PEDAL, softness);
            	case 25: return new DrumStrike(ControlType.RED, softness);
            	case 17: return new DrumStrike(ControlType.ORANGE, softness);
            	case 15: return new DrumStrike(ControlType.BLUE, softness);
            	case 14: return new DrumStrike(ControlType.YELLOW, softness);
            	case 18: return new DrumStrike(ControlType.GREEN, softness);
            }
        }
        
        return null;

		//m_SX =  p_Data0 & 63;
        //m_SY = p_Data1] & 63;
        //m_HighHatPedal = (p_Data2 & 128) == 0;
        //m_Orange = (p_Data5 & 128) == 0;
        //m_Red = (p_Data5 & 64) == 0;
        //m_Yellow = (p_Data5 & 32) == 0;
        //m_Green = (p_Data5 & 1) == 0;
        //m_Blue = (p_Data5 & 8) == 0;
        //m_Pedal = (p_Data5 & 4) == 0;
	}
	
/*	
	public String getRawString () {
		return bytesToHex(m_Raw);
	}
	
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars);
	}
*/	
}
