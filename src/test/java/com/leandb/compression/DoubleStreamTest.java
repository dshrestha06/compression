package com.leandb.compression;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by danish on 3/1/16.
 */
public class DoubleStreamTest {
    @Test
    public void testDoubleStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DoubleStream dStream = new DoubleStream(bos);
        dStream.add(12.0);
        dStream.add(12.1);
        dStream.add(12.1);
        dStream.add(13.0);
        dStream.add(13.1);
        dStream.add(25);
        dStream.flush();
        byte[] arr = bos.toByteArray();

        DoubleStreamReader reader = DoubleStreamReader.from(new ByteArrayInputStream(arr));
        while(reader.hasNext()) {
            System.out.println(reader.next());
        }
    }

}
