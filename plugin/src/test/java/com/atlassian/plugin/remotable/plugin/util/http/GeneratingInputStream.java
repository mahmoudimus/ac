package com.atlassian.plugin.remotable.plugin.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 *
 */
public class GeneratingInputStream extends InputStream
{
    private final long maxLength;
    private final byte data;

    private long counter;

    public GeneratingInputStream(char character, long maxLength)
    {
        this.maxLength = maxLength;
        this.data = String.valueOf(character).getBytes(Charset.forName("UTF-8"))[0];
    }

    @Override
    public int read() throws IOException
    {
        if (++counter <= maxLength)
        {
            return data;
        }
        else
        {
            return -1;
        }
    }
}
