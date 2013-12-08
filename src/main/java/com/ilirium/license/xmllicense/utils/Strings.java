package com.ilirium.license.xmllicense.utils;

/**
 *
 * @author DoDo <dopoljak@gmail.com>
 */
public class Strings
{
    private static char[] HEX = "0123456789ABCDEF".toCharArray();

    /**
     * Byte array to HEX String
     */
    public static String toHEX(byte[] input)
    {
        if (input == null) {
            return null;
        }

        int length = input.length;
        char[] output = new char[2 * length];
        for (int i = 0; i < length; ++i) {
            output[2 * i] = HEX[(input[i] & 0xF0) >>> 4];
            output[2 * i + 1] = HEX[input[i] & 0x0F];
        }
        return new String(output);
    }
    
    /**
     * Byte to Hex String
     */
    public static String toHEX(byte input)
    {
	char[] output = new char[2];
	output[0] = HEX[(input & 0xF0) >>> 4];
	output[1] = HEX[input & 0x0F];
	return new String(output);
    }
    
    /**
     * Hex String to Byte array
     */
    public static byte[] fromHEX(String hexInput)
    {
	if (hexInput == null)
	{
	    return null;
	}

	int length = hexInput.length();
	byte[] output = new byte[length / 2];
	for (int i = 0; i < length; i += 2)
	{
	    output[i / 2] = (byte) ((Character.digit(hexInput.charAt(i), 16) << 4) + Character.digit(hexInput.charAt(i + 1), 16));
	}
	return output;
    }
}
