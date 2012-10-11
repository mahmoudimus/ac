package com.atlassian.plugin.remotable.container.service.sal;

import java.util.HashMap;
import java.util.Map;

/**
 * This class helps substitute characters that are not supported in xml (see all the chars defined in escapeString variable) to escaped format.
 * Its implementation is based on $FISHEYE_TRUNK/src/java/com/cenqua/fisheye/util/XmlUtils.java.
 *
 * @since 2.7.0
 */
public class XmlEncodingUtils
{

    private static final char ESCAPE_SYM1 = '!';
    private static final char ESCAPE_SYM2 = '@';
    private static final char ESCAPE_SYM3 = '#';
    private static final char ESCAPE_SYM4 = '$';
    private static final Map<Character, String> charToCode;

    static
    {
        final String escapeString = "\u0000\u0001\u0002\u0003\u0004\u0005" +
            "\u0006\u0007\u0008\u000B\u000C\u000E\u000F\u0010\u0011\u0012" +
            "\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C" +
            "\u001D\u001E\u001F\uFFFE\uFFFF";

        charToCode = new HashMap<Character, String>();

        for (int i = 0; i < escapeString.length(); i++)
        {
            final char c = escapeString.charAt(i);
            charToCode.put(c, "" + ESCAPE_SYM1 + ESCAPE_SYM2
                                + ESCAPE_SYM3 + ESCAPE_SYM4
                                + String.format("%04X", (int) c));
        }
    }

    // Not for instantiation.
    private XmlEncodingUtils()
    {
    }

    /**
     * Replaces all characters that are illegal in XML with a custom-made unicode escape sequence.
     * escape sequence '!@#$[0-9][0-9][0-9][0-9]'. When <code>null</code> is
     * passed into this method, <code>null</code> is returned.
     *
     * @param string
     * @return
     */
    public static String escape(String string)
    {
        if (string == null)
        {
            return null;
        }

        // working buffer.
        StringBuilder copy = new StringBuilder();

        // just consider every char for a chance of escaping.
        for (int i = 0; i < string.length(); i++)
        {
            copy.append(escapeChar(string.charAt(i)));
        }
        return copy.toString();
    }

    private static String escapeChar(char c)
    {
        String escaped = charToCode.get(c);
        // escape the char if its escape sequence is defined.
        return escaped == null ? String.valueOf(c):escaped;
    }

    /**
     * Substitutes all occurances of '!@#$[0-9][0-9][0-9][0-9]' with their
     * corresponding character codes. When <code>null</code> is passed into this
     * method, <code>null</code> is returned.
     *
     * @param string
     * @return
     */
    public static String unescape(String string) {

        if (string == null) {
            return null;

        } else {

            // the conversion buffer.
            final StringBuilder copy = new StringBuilder();

            for (int i = 0; i < string.length(); i++)
            {
                char c = string.charAt(i);
                // if there is a possibility that this is an escape sequence (subject to the parsability of the code part).
                if (i+7 < string.length()
                    && c == ESCAPE_SYM1
                    && string.charAt(i+1) == ESCAPE_SYM2
                    && string.charAt(i+2) == ESCAPE_SYM3
                    && string.charAt(i+3) == ESCAPE_SYM4)
                {
                    // extract the number code
                    String value = string.substring(i + 4, i + 8);
                    try
                    {
                        // if this is parsable fine, we just convert it to the original.
                        int charCode = Integer.parseInt(value, 16);
                        copy.append((char) charCode);
                        i += 7;
                    }
                    catch (NumberFormatException nfe)
                    {
                        // if it cannot be parsed to base 16, then this is not an escape sequence
                        // we simply append the char being read to the buffer.
                        copy.append(c);
                    }
                }
                // otherwise, just append the char we just read to the buffer.
                else
                {
                    copy.append(c);
                }
            }

            return copy.toString();
        }
    }
}

