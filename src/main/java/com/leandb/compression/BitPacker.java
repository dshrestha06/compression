package com.leandb.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by danish on 2/28/16.
 */
public class BitPacker {
    short length;
    byte[] buffer;
    short index;
    OutputStream os;

    byte currentByte = 0x00;
    short currentBitPos = 0;

    public BitPacker(OutputStream os) {
        length = 1024;
        buffer = new byte[length];
        this.os = os;
    }


    private void writeByte(byte b) {
        writeBits(b, (short) 8);
    }

    public void writeBits(byte b, short bitLength) {
        if(bitLength > 8)
            bitLength = 8;  //there cant be more than 8 bits in a byte

        if(currentBitPos + bitLength <= 8) {
            currentByte |= b << (8- currentBitPos - bitLength);
            currentBitPos += bitLength;
        }else {
            //write to remaining space
            if(bitLength <= currentBitPos)
                currentByte |= b >>> (currentBitPos - bitLength);
            else if((byte) (b & (byte) 0x80) == (byte) 0x80) //negative #
                currentByte |= b >>> (currentBitPos - bitLength);
            else
               currentByte |= b >>> (8 + currentBitPos - bitLength);
            bitLength = (short) (bitLength - 8 + currentBitPos);
            copyCurrentByteToBuffer();

            currentByte |= b << (8- currentBitPos - bitLength);
            currentBitPos += bitLength;
        }

        if(currentBitPos == 8) {
            //write to buffer
            copyCurrentByteToBuffer();
        }
    }

    public void writeInt(int val) throws IOException {
        writeInt(val, (short) 32);  //32 bits
    }

    public void writeInt(int val, short bitLength) throws IOException {
        byte[] arr = new byte[4];
        ByteBuffer.wrap(arr).putInt(val);
        writeBytes(arr, bitLength);
    }

    private void writeBytes(byte[] arr, short bitLength) throws IOException {
        int startPos = (int) (arr.length - Math.ceil(bitLength / 8.0));
        for (int i = startPos; i < arr.length; i++) {
            if (bitLength % 8 != 0) {
                if(i== startPos) {
                    writeBits(arr[i], (short) (bitLength % 8));
                }else {
                    writeByte(arr[i]);
                }

            } else {
                //write Byte
                writeByte(arr[i]);
            }
        }
    }

    public void writeLong(long val) throws IOException {
        byte[] arr = new byte[8];
        ByteBuffer.wrap(arr).putLong(val);
        writeBytes(arr, (short) 64);
    }

    public void flush() throws IOException {
        copyCurrentByteToBuffer();
        os.write(buffer, 0, index);
        os.flush();

        buffer[index-1]=0x00;
        index = 0;
    }

    private void copyCurrentByteToBuffer() {
        if(currentBitPos!=0) {
            buffer[index++] = currentByte;
            currentByte = 0x00;
            currentBitPos = 0;
        }
    }
}