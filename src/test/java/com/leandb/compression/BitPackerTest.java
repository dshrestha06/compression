package com.leandb.compression;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by danish on 2/28/16.
 */
public class BitPackerTest {

    ByteArrayOutputStream bos;
    BitPacker bitPacker;

    @Before
    public void reset() {
        bos = new ByteArrayOutputStream();
        bitPacker = new BitPacker(bos);
    }

    @Test
    public void testWriteBits() throws IOException {
        bitPacker.writeBits((byte) 0x03, (short) 2);// 11
        bitPacker.writeBits((byte) 0x01, (short) 2);// 01

        //result should be a single byte 1001
        byte[] arr = getBytes();
        Assert.assertEquals(1, arr.length);
        Assert.assertEquals("11010000", ByteUtils.byteToBits(arr[0]));

        reset();
        bitPacker.writeBits((byte) 0x00, (short) 4);// 0000
        bitPacker.writeBits((byte) 0xF, (short) 4);// 1111
        arr = getBytes();
        Assert.assertEquals(1, arr.length);
        Assert.assertEquals("00001111", ByteUtils.byteToBits(arr[0]));
    }

    @Test
    public void testWriteBitsOverflow() throws IOException {
        bitPacker.writeBits((byte) 0x10, (short) 5);// 10000
        bitPacker.writeBits((byte) 0xF, (short) 4);// 1111
        byte[] arr = getBytes();
        Assert.assertEquals(2, arr.length);
        Assert.assertEquals("10000111", ByteUtils.byteToBits(arr[0]));
        Assert.assertEquals("10000000", ByteUtils.byteToBits(arr[1]));

        reset();
        bitPacker.writeBits((byte) 0x00, (short) 1);// 0
        bitPacker.writeBits((byte) 0xFF, (short) 8);// 1111 1111
        arr = getBytes();
        Assert.assertEquals(2, arr.length);
        Assert.assertEquals("01111111", ByteUtils.byteToBits(arr[0]));
        Assert.assertEquals("10000000", ByteUtils.byteToBits(arr[1]));
    }

    @Test
    public void testWriteBytesWithStartOffset() throws IOException {
        byte[] arr = new byte[4];
        arr[0] = (byte) 0x00;
        arr[1] = (byte) 0x23;
        arr[2] = (byte) 0xF3;
        arr[3] = (byte) 0xF0;

        bitPacker.writeBytes(arr, (short) 10, (short) 18);

        byte[] result = getBytes();
        Assert.assertEquals(3, result.length);
        Assert.assertEquals("10001111", ByteUtils.byteToBits(result[0]));
        Assert.assertEquals("11001111", ByteUtils.byteToBits(result[1]));
        Assert.assertEquals("11000000", ByteUtils.byteToBits(result[2]));

        reset();
        bitPacker.writeBytes(arr, (short) 10, (short) 16);
        result = getBytes();
        Assert.assertEquals(2, result.length);
        Assert.assertEquals("10001111", ByteUtils.byteToBits(result[0]));
        Assert.assertEquals("11001111", ByteUtils.byteToBits(result[1]));


        reset();
        bitPacker.writeBytes(arr, (short) 10, (short) 14);
        result = getBytes();
        Assert.assertEquals(2, result.length);
        Assert.assertEquals("10001111", ByteUtils.byteToBits(result[0]));
        Assert.assertEquals("11001100", ByteUtils.byteToBits(result[1]));
    }

    @Test
    public void testwriteInt() throws IOException {
        bitPacker.writeInt(Integer.MAX_VALUE);
        byte[] arr = getBytes();
        Assert.assertEquals("01111111", ByteUtils.byteToBits(arr[0]));
        Assert.assertEquals("11111111", ByteUtils.byteToBits(arr[1]));
        Assert.assertEquals("11111111", ByteUtils.byteToBits(arr[2]));
        Assert.assertEquals("11111111", ByteUtils.byteToBits(arr[3]));

        reset();
        //test Overflow
        bitPacker.writeBits((byte) 0x00, (short) 1);
        bitPacker.writeInt(Integer.MAX_VALUE);
        arr = getBytes();
        Assert.assertEquals("00111111", ByteUtils.byteToBits(arr[0]));
        Assert.assertEquals("11111111", ByteUtils.byteToBits(arr[1]));
        Assert.assertEquals("11111111", ByteUtils.byteToBits(arr[2]));
        Assert.assertEquals("11111111", ByteUtils.byteToBits(arr[3]));
        Assert.assertEquals("10000000", ByteUtils.byteToBits(arr[4]));

    }

    @Test
    public void testwriteIntVariableBits() throws IOException {
        bitPacker.writeInt(256, (short) 9);
        byte[] arr = getBytes();
        Assert.assertEquals("10000000", ByteUtils.byteToBits(arr[0]));
        Assert.assertEquals("00000000", ByteUtils.byteToBits(arr[1]));

        reset();
        bitPacker.writeInt(-1, (short) 9);
        arr = getBytes();
        Assert.assertEquals("11111111", ByteUtils.byteToBits(arr[0]));
        Assert.assertEquals("10000000", ByteUtils.byteToBits(arr[1]));

        reset();
        //test Overflow
        bitPacker.writeBits((byte) 0x00, (short) 1);
        bitPacker.writeInt(256, (short) 9);
        arr = getBytes();
        Assert.assertEquals("01000000", ByteUtils.byteToBits(arr[0]));
        Assert.assertEquals("00000000", ByteUtils.byteToBits(arr[1]));
    }

    public byte[] getBytes() throws IOException {
        bitPacker.flush();
        return bos.toByteArray();
    }
}
