package com.leandb.compression;

/**
 * Created by danish on 2/28/16.
 */
public class ByteUtils {
    private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String byteToBits(byte b)  {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }

    public static String bytesToHex(byte[] bytes)  {
        char[] hexChars = new char[bytes.length * 2];
        for (int j=0; j<bytes.length; j++) {
            byte v = (byte) (bytes[j] & 0xFF);
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
