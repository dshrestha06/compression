package com.leandb.compression;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by danish on 3/3/16.
 */
public class BitUnpackerTest {
    ByteArrayOutputStream bos;
    BitPacker bitPacker;
    BitUnpacker unpacker;

    byte[] arr = new byte[2];

    @Before
    public void setup() throws IOException {
        bos = new ByteArrayOutputStream();
        bitPacker = new BitPacker(bos);
        bitPacker.writeBits((byte) 0x01, (short) 1);
        bitPacker.writeLong(23L);
        bitPacker.flush();

        arr[0] = (byte) 0xAA;
        arr[1] = (byte) 0x7A;

        unpacker = new BitUnpacker(new ByteArrayInputStream(bos.toByteArray()));
    }

    public void reset() {

    }

    @Test
    public void readLong() throws IOException {
        byte[] arr = new byte[8];
        ByteBuffer.wrap(arr).putLong(23L);
        BitUnpacker unpacker = new BitUnpacker(new ByteArrayInputStream(arr));
        Assert.assertEquals(23L, unpacker.readLong());
    }

    @Test
    public void readBit() throws IOException {
        byte bit = unpacker.readBit();
        Assert.assertEquals("00000001", ByteUtils.byteToBits(bit));

        unpacker = new BitUnpacker(new ByteArrayInputStream(arr));
        System.out.println(ByteUtils.byteToBits(arr[0]));
        System.out.println(ByteUtils.byteToBits(arr[1]));
        Assert.assertEquals("00000001", ByteUtils.byteToBits(unpacker.readBit()));
        Assert.assertEquals("00000000", ByteUtils.byteToBits(unpacker.readBit()));
        Assert.assertEquals("00000001", ByteUtils.byteToBits(unpacker.readBit()));
        Assert.assertEquals("00000000", ByteUtils.byteToBits(unpacker.readBit()));
        Assert.assertEquals("00000001", ByteUtils.byteToBits(unpacker.readBit()));
        Assert.assertEquals("00000000", ByteUtils.byteToBits(unpacker.readBit()));
        Assert.assertEquals("00000001", ByteUtils.byteToBits(unpacker.readBit()));
        Assert.assertEquals("00000000", ByteUtils.byteToBits(unpacker.readBit()));

        Assert.assertEquals("00000000", ByteUtils.byteToBits(unpacker.readBit()));
        Assert.assertEquals("00000001", ByteUtils.byteToBits(unpacker.readBit()));
        Assert.assertEquals("00000001", ByteUtils.byteToBits(unpacker.readBit()));
        Assert.assertEquals("00000001", ByteUtils.byteToBits(unpacker.readBit()));
    }

    @Test
    public void readBits() throws IOException {
        unpacker = new BitUnpacker(new ByteArrayInputStream(arr));
        Assert.assertEquals("00000101", ByteUtils.byteToBits(unpacker.readBits(3)));
        Assert.assertEquals("00001010", ByteUtils.byteToBits(unpacker.readBits(5)));

        unpacker = new BitUnpacker(new ByteArrayInputStream(arr));
        Assert.assertEquals("00000101", ByteUtils.byteToBits(unpacker.readBits(3)));
        Assert.assertEquals("00101001", ByteUtils.byteToBits(unpacker.readBits(7)));

        unpacker = new BitUnpacker(new ByteArrayInputStream(arr));
        Assert.assertEquals("00000010", ByteUtils.byteToBits(unpacker.readBits(2)));
        Assert.assertEquals("10101001", ByteUtils.byteToBits(unpacker.readBits(8)));
    }

    @Test
    public void readLongOverflow() throws IOException {
        byte bit = unpacker.readBit();
        Assert.assertEquals("00000001", ByteUtils.byteToBits(bit));
        Assert.assertEquals(23L, unpacker.readLong());
    }
}
