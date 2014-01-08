package com.atlassian.plugin.connect.plugin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.google.common.base.Preconditions.checkNotNull;

public class StreamUtil
{
    private static final Logger log = LoggerFactory.getLogger(StreamUtil.class);

    /**
     * Convert InputStream to String
     * @param inputStream {@link InputStream to read}
     * @return {@link String} content of inputStream
     * @throws IOException
     */
    public static String getStringFromInputStream(@Nonnull InputStream inputStream) throws IOException
    {
        checkNotNull(inputStream);
        BufferedReader bufReader = null;
        StringBuilder sb = new StringBuilder();
        String line;

        try
        {
            bufReader = new BufferedReader(new InputStreamReader(inputStream));

            while ((line = bufReader.readLine()) != null)
            {
                sb.append(line);
            }
        }
        finally
        {
            if (bufReader != null)
            {
                try
                {
                    bufReader.close();
                }
                catch (IOException e)
                {
                    log.error("Failed to close BufferedReader. Ignoring!", e);
                }
            }
        }

        return sb.toString();
    }
}