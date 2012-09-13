package com.atlassian.labs.remoteapps.plugin.util;

import com.atlassian.renderer.v2.components.HtmlEscaper;
import org.apache.commons.codec.binary.Base64;

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

    public static String escapeQuotes(String value)
    {
        return value.replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String escapeAll(String value)
    {
        return HtmlEscaper.escapeAll(value, true);
    }
}
