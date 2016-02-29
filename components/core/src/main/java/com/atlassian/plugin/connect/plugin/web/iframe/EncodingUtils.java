package com.atlassian.plugin.connect.plugin.web.iframe;

public class EncodingUtils {
    private EncodingUtils() {
    }

    public static String escapeQuotes(String value) {
        return value.replace("\"", "&quot;").replace("'", "&#39;");
    }
}
