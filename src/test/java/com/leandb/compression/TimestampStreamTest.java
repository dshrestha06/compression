package com.leandb.compression;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by danish on 3/1/16.
 */
public class TimestampStreamTest {
    @Test
    public void testTimestampStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TimestampStream tsStream = new TimestampStream(bos);
        long time =14570510000000L;

        tsStream.addTimestamp(time);
        tsStream.addTimestamp(time + 10);
        tsStream.addTimestamp(time + 100);
        tsStream.addTimestamp(time + 1000);
        tsStream.addTimestamp(time + 10000);
        tsStream.addTimestamp(time + 100000);
        tsStream.addTimestamp(time + 1000000);
        tsStream.addTimestamp(time + 10000000);
        tsStream.addTimestamp(time + 100000000);

        tsStream.flush();
        byte[] arr = bos.toByteArray();

        TimestampStreamReader reader = TimestampStreamReader.from(new ByteArrayInputStream(arr));
        long x = 0;
        while(reader.hasNext()) {
            System.out.println(reader.next());

        }

    }
}
