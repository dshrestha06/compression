package com.leandb.compression;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Created by danish on 3/1/16.
 */
public class DoubleStreamReader implements Iterator<Double> {
    private BitUnpacker bitUnpacker;
    private boolean started = false;
    private double previousValue;
    private int previousLeadingSize = 0;
    private int previousLength = 0;

    public DoubleStreamReader(InputStream in) throws IOException {
        bitUnpacker = new BitUnpacker(in);
    }

    public static DoubleStreamReader from(InputStream in) throws IOException {
        return new DoubleStreamReader(in);
    }

    @Override
    public boolean hasNext() {
        try {
            return bitUnpacker.hasNext();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Double next() {
        try {
        if(!started) {
                previousValue = bitUnpacker.readDouble();
                started = true;
        } else {
            byte bit = bitUnpacker.readBits(1);
            if(bit == 0x00) {
                //current value is previous Value
            } else {
                //bit should be 0x01
                bit = bitUnpacker.readBits(1);

                //meaningful bits aligned
                if(bit == 0x00) {
                    double currentValue = readDouble(previousLeadingSize, previousLength, previousValue, bitUnpacker);
                    previousValue = currentValue;
                } else {
                    int leadingZeros = bitUnpacker.readInt(5);
                    int length = bitUnpacker.readInt(6);
                    double currentValue = readDouble(leadingZeros, length,  previousValue, bitUnpacker);

                    previousLeadingSize = leadingZeros;
                    previousLength = length;
                    previousValue = currentValue;
                }
            }
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return previousValue;
    }

    private double readDouble(int leadingZeros, int length, double previousValue, BitUnpacker bitUnpacker) throws IOException {
        byte[] currentValueBytes = new byte[8];

        byte[] xor = new byte[8];
        byte[] notXor = new byte[8];
        byte[] previousValueBytes = new byte[8];
        ByteBuffer.wrap(previousValueBytes).putDouble(previousValue);

        int currentIndex = leadingZeros / 8;
        int currentPos = leadingZeros % 8;
        while(length >= 8) {
            xor[currentIndex] = bitUnpacker.readBits(8 - currentPos);
            currentIndex++;
            length = length - 8 + currentPos;
            currentPos = 0;
        }
        if(length>0) {
            xor[currentIndex] = (byte) (bitUnpacker.readBits(length) << (8-length));
            currentIndex++;

        }

        for(int i=0;i<previousValueBytes.length;i++) {
            notXor[i] = (byte) (xor[i] ^ 0xFF);

            //A & mask (leading + xor) 11110011
            //A' & mask (xor)          00001100
            //(A & mask) | (A' & mask')
            byte similarBits = (byte) (previousValueBytes[i] & notXor[i]);   //when xor is zero grab the previous Value Bytes
            byte nonSimilarBits = (byte) ((previousValueBytes[i]^ 0xFF) & xor[i]);   //when xor is zero grab the previous Value Bytes
            currentValueBytes[i] = (byte) (similarBits | nonSimilarBits);
        }

        return ByteBuffer.wrap(currentValueBytes).getDouble();
    }
}
