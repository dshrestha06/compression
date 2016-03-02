package com.leandb.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by danish on 2/27/16.
 */
public class DoubleStream {
    private byte[] previousBytes;
    private byte[] previousXorBytes;
    private boolean started = false;

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    BitPacker bitPacker = new BitPacker(bos);

    public void add(double value) throws IOException {
        byte[] currentBytes = new byte[8];
        ByteBuffer.wrap(currentBytes).putDouble(value);

        //store the first value as is
        if (!started) {
            started = true;
            bitPacker.writeDouble(value);
            previousXorBytes = new byte[8]; //start a fresh xor bytes
        } else {
            byte[] xor = new byte[8];
            boolean isAllZeros = calculateXor(previousBytes, currentBytes, xor);


            //if XOR with previous value is 0 (same value) store ‘0’ bit
            if(isAllZeros) {
                bitPacker.writeBits((byte) 0x00, (short) 1);
            } else {
                byte[] xorWithPrevious = new byte[8];
                boolean meaningfulBitsAligned = calculateXor(previousXorBytes, xor, xorWithPrevious);

                int leadingZeros = ByteUtils.leadingZeros(xor);
                int trailingZeros = ByteUtils.trailingZeros(xor);
                int length = 64 - leadingZeros - trailingZeros;

                if(meaningfulBitsAligned) {
                    bitPacker.writeBits((byte) 0x00, (short) 1);
                    //write meaningful xor bits
                    bitPacker.writeBytes(xor, (short) leadingZeros, (short) length);
                }else {
                    bitPacker.writeBits((byte) 0x01, (short) 1);
                    //length of leading zeros in 5 bits
                    bitPacker.writeInt(leadingZeros, (short) 5);

                    //length of meaninful bits in 6 bits
                    bitPacker.writeInt(length, (short) 6);

                    //write meaningful xor bits
                    bitPacker.writeBytes(xor, (short) leadingZeros, (short) length);

                }
            }
            previousXorBytes = xor;
        }

        previousBytes = currentBytes;
    }

    private boolean calculateXor(byte[] previousBytes, byte[] currentBytes, byte[] xor) {
        boolean isAllZeros = true;
        for ( int i =0 ; i<xor.length; i++) {
            xor[i] = (byte) (previousBytes[i] ^ currentBytes[i]);
            isAllZeros = isAllZeros && (xor[i] == 0x00);
        }
        return isAllZeros;
    }

    public void flush() throws IOException {
        bitPacker.flush();
    }
}