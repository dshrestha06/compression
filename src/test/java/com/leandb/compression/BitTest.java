package com.leandb.compression;

import java.nio.ByteBuffer;

/**
 * Created by danish on 2/27/16.
 */
public class BitTest {
    public static void main(String[] args) {
        byte[] integer = new byte[4];
        ByteBuffer.wrap(integer).putInt(2048);
        System.out.println(Integer.toBinaryString(2048));
        System.out.println(Integer.toBinaryString(-2047));


        byte[] twelve = new byte[8];
        byte[] twentyFour = new byte[8];
        byte[] xor = new byte[8];
        ByteBuffer.wrap(twelve).putDouble(12.0);
        ByteBuffer.wrap(twentyFour).putDouble(24.0);

        for ( int i =0 ; i<xor.length; i++) {
            xor[i] = (byte) (twelve[i] ^ twentyFour[i]);
        }

        System.out.println(ByteUtils.bytesToHex(twelve));
        System.out.println(ByteUtils.bytesToHex(twentyFour));
        System.out.println(ByteUtils.bytesToHex(xor));

        byte buffer = 0x00;
        short bufferPos=0;
        System.out.println(ByteUtils.byteToBits(buffer));

        buffer |=  (byte)(0x01 << 7);
        bufferPos++;

        System.out.println(ByteUtils.byteToBits(buffer));
    }


}
