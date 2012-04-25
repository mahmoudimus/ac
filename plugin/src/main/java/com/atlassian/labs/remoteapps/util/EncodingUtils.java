package com.atlassian.labs.remoteapps.util;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class EncodingUtils
{
    public static String encodeBase64(String text)
    {
        if (text == null)
        {
            return "";
        }
        else
        {
            byte[] bytes = text.getBytes(Charset.defaultCharset());
            return new String(Base64.encodeBase64(bytes));
        }
    }

    /**
     * A utility method to encode HTTP form parameter/values.
     * To encode entire URLs, use completeUrlEncode() instead.
     *
     * Copied from Confluence 4.2 GeneralUtil
     * @param url - part of the url to encode
     * @return encoded url
     */
    public static String urlEncode(String url)
    {
        if (url == null)
        {
            return null;
        }

        return urlEncode(url, "UTF-8");
    }

    private static String urlEncode(String value, String encoding)
    {
        String s;
        try
        {
            s = URLEncoder.encode(value, encoding);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }

        // Don't encode the "@" and the "~" character to make personal space URLs look nice
        s = s.replaceAll("%40", "@").replaceAll("%7E", "~");

        return s;
    }
}
