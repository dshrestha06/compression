package com.leandb.compression;

import java.nio.ByteBuffer;

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


    public static int leadingZeros(byte[] arr) {
        int count = 0;
        int i = 0;
        do {
            count = count + leadingZeros(arr[i++]);
        }while(i<arr.length-1 && count % 8 ==0);
        return count;
    }

    public static int leadingZeros(byte b) {
        int count = 0;
        for(int i=0;i<=7;i++) {
            if((byte) (0x80 & (b << i)) == (byte) 0x00) {
                count ++;
            }else {
                break;
            }
        }
        return count;
    }

    public static int trailingZeros(byte[] arr) {
        int count = 0;
        int lastTrailingZeros = 0;
        int i = arr.length-1;
        do {
            lastTrailingZeros = trailingZeros(arr[i--]);
            count = count + lastTrailingZeros;
        }while(i>=0 && lastTrailingZeros==8);
        return count;
    }

    public static int trailingZeros(byte b) {
        int count = 0;
        for(int i=0;i<=7;i++) {
            if((byte) (0x01 & (b >> i)) == (byte) 0x00) {
                count ++;
            }else {
                break;
            }
        }
        return count;
    }



    public static void print(double v) {
        byte[] arr = new byte[8];
        ByteBuffer.wrap(arr).putDouble(v);
        printBytes(arr);
    }

    public static void printBytes(byte[] arr) {
        for(byte b : arr)
            System.out.print(ByteUtils.byteToBits(b) + " ");
        System.out.println("");
    }
}
