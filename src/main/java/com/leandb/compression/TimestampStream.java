package com.leandb.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by danish on 2/27/16.
 */
public class TimestampStream {
    long startTime;
    long previousTimestamp;
    long lastDelta = 0;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    BitPacker bitPacker = new BitPacker(bos);

    public void addTimestamp(long timestamp) throws IOException {
        //store the first timestamp as is
        if (startTime == 0) {
            bitPacker.writeLong(timestamp);
            startTime = timestamp;
        } else {
            long delta = timestamp - previousTimestamp;
            long deltaOfDelta = delta - lastDelta;  //delta of delta is same as delta for the second value. Need to consider this when reading.

            if(deltaOfDelta == 0) {
                //if D == 0 then store '0'
                bitPacker.writeBits((byte) 0x00, (short) 1);
            } else if (deltaOfDelta >=-63 && deltaOfDelta <=64) {
                //if D is between [-63,64] store '10' followed by the value (7 bit)
                bitPacker.writeBits((byte) 0x10, (short) 2);
                bitPacker.writeInt((int) deltaOfDelta, (short) 7);
            }else if (deltaOfDelta >=-255 && deltaOfDelta <=256) {
                //if D is between [[-255, 256] store '110' followed by the value (9 bit)
                bitPacker.writeBits((byte) 0x110, (short) 3);
                bitPacker.writeInt((int) deltaOfDelta, (short) 9);
            }else if (deltaOfDelta >=-2047 && deltaOfDelta <=2048) {
                //if D is between [-2047, 2048] store '1110' ’ followed by the value (12 bits)
                bitPacker.writeBits((byte) 0x1110, (short) 4);
                bitPacker.writeInt((int) deltaOfDelta, (short) 12);
            }else if (deltaOfDelta >=Integer.MIN_VALUE && deltaOfDelta <=Integer.MAX_VALUE) {
                bitPacker.writeBits((byte) 0x11110, (short) 5);
                bitPacker.writeInt((int) deltaOfDelta);
            }else {
                //write long. Gorilla timestamp compression doesnt have a case for this.
                bitPacker.writeBits((byte) 0x11111, (short) 5);
                bitPacker.writeLong(deltaOfDelta);
            }
            lastDelta = delta;
        }

        previousTimestamp = timestamp;
    }

    public void flush() throws IOException {
        bitPacker.flush();
    }
}