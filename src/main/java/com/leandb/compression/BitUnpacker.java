package com.leandb.compression;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by danish on 3/3/16.
 */
public class BitUnpacker {
    InputStream in;
    byte[] buffer;
    int bufferIndex = 0;
    int bufferRead = 0;
    int currentBytePos;

    public long readLong() throws IOException {
        hydrate();
        byte[] data;
        if(currentBytePos % 8 == 0) {
            data  = copyBytes(8);
        }else {
            data = readBytes(64);
        }
        return ByteBuffer.wrap(data).getLong();
    }

    public double readDouble() throws IOException {
        hydrate();
        byte[] data;
        if(currentBytePos % 8 == 0) {
            data  = copyBytes(8);
        }else {
            data = readBytes(64);
        }
        return ByteBuffer.wrap(data).getDouble();
    }

    private byte[] copyBytes(int size) {
        byte[] data = new byte[size];
        System.arraycopy(buffer, bufferIndex, data, 0, bufferIndex + size);
        bufferIndex = bufferIndex + size;
        return data;
    }

    public int readInt() throws IOException {
        hydrate();
        byte[] data;
        if(currentBytePos % 8 == 0) {
            data  = copyBytes(4);
        }else {
            data = readBytes(32);
        }
        return ByteBuffer.wrap(data).getInt();
    }

    public int readInt(int bits) throws IOException {
        hydrate();
        byte[] arr = resize(readBytes(bits), 4);
        return ByteBuffer.wrap(arr).getInt();
    }

    private byte[] resize(byte[] bytes, int size) {
        if(bytes.length == size)
            return bytes;
        else {
            byte[] target = new byte[size];
            int startPos = size - bytes.length;
            for(int i = 0; i<target.length;i++) {
                if(i<startPos) {
                    target[i] = 0x00;
                }else if(i >= startPos) {
                    target[i] = bytes[i-startPos];
                }
            }
            return target;
        }
    }


    public BitUnpacker(InputStream in) throws IOException {
        this.in = in;
        buffer = new byte[1024]; //hardcoded size for now
    }

    public byte readByte() throws IOException {
        return readBits(8);
    }

    public byte readBit() throws IOException {
        return readBits(1);
    }


    /**
     *
     * @param length is assumed to be no more than 8
     * @return
     * @throws IOException
     */
    public byte readBits(int length) throws IOException {
        hydrate();

        if(currentBytePos + length <= 8) {
            byte toReturn = buffer[bufferIndex];

            byte mask = 0x01;
            for(int i=1;i<length;i++) {
                mask =(byte) ((mask << 1) | 0x01);
            }
            mask = (byte) (mask << (8-currentBytePos-length));
            toReturn = (byte) (toReturn & mask);
            toReturn = (byte) ((toReturn & 0xFF) >>> (8-currentBytePos-length));
            currentBytePos = currentBytePos + length;
            if(currentBytePos >= 8) {
                fetchNextByte();
            }
            return toReturn;
        } else {
            //need to split.
            int firstPartSize = 8 - currentBytePos;
            int secondPartSize = length - firstPartSize;
            byte firstPart = readBits(firstPartSize);
            byte secondPart = readBits(secondPartSize);
            return (byte) (firstPart << (length - firstPartSize  ) | secondPart);
        }
    }


    public byte[] readBytes(int length) throws IOException {
        int size = length/8;
        if(length % 8 != 0)
            size ++;

        byte[] toReturn = new byte[size];
        int i = 0;
        if(length % 8 != 0) {
            toReturn[i++] = readBits(length % 8);   //read partial to ensure all the padding is included in the first byte.
            // otherwise padding will be included in the last byte.
            length = length - length % 8;
        }

        while(length > 0) {
            toReturn[i++] = readByte();
            length = length - 8;
        };


        return toReturn;
    }

    public void hydrate() throws IOException {
        if (bufferRead == 0 || bufferIndex >= bufferRead) {
            this.bufferRead = in.read(buffer);
            bufferIndex = 0;
            currentBytePos = 0;
        }
    }

    private void fetchNextByte() throws IOException {
        bufferIndex++;
        currentBytePos = 0;
        hydrate(); //hydrate if bufferIndex is beyond the buffered array limit
    }

    public boolean hasNext() throws IOException {
        hydrate();

        if(bufferIndex >= bufferRead && currentBytePos %8 == 0) {
            return in.available() > 0;
        }else if(bufferIndex >= (bufferRead -1 )) {
            return in.available() > 0;
        }

        return true;
    }
}
