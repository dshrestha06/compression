package com.leandb.compression;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Created by danish on 3/1/16.
 */
public class TimestampStreamReader implements Iterator<Long> {
    private BitUnpacker bitUnpacker;
    private boolean started = false;
    private long previousValue;
    private long previousDelta;

    public TimestampStreamReader(InputStream in) throws IOException {
        bitUnpacker = new BitUnpacker(in);
    }

    public static TimestampStreamReader from(InputStream in) throws IOException {
        return new TimestampStreamReader(in);
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
    public Long next() {
        try {
        if(!started) {
                previousValue = bitUnpacker.readLong();
                started = true;
        } else {
            byte header = bitUnpacker.readBits(1);
            if(header == 0x0) {
                //the delta is zero, leave the previous value as is
                previousDelta = 0;
            }else {
                int size = getSizeFromHeader();
                long currentDelta;
                if(size <=32) {
                    currentDelta = bitUnpacker.readInt(size);
                }else {
                    currentDelta =  bitUnpacker.readLong();
                }
                long delta = previousDelta + currentDelta;
                long currentValue = previousValue + delta;
                previousValue = currentValue;
                previousDelta = previousDelta + currentDelta;
            }
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return previousValue;
    }

    public int getSizeFromHeader() throws IOException {
        byte header = bitUnpacker.readBits(1);
        int size;
        //10
        if(header == 0x00) {
            size = 7;
        } else {
            header = bitUnpacker.readBits(1);
            //110
            if(header == 0x00) {
                size = 9;
            } else {
                header = bitUnpacker.readBits(1);
                //1110
                if(header == 0x00) {
                    size = 12;
                } else {
                    header = bitUnpacker.readBits(1);
                    //11110
                    if(header == 0x00) {
                        size = 32;
                    } else {
                        size = 64;
                    }
                }
            }
        }
        return size;
    }
}
