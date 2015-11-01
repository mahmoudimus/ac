package com.atlassian.plugin.connect.test.utils;

public class IframeUtils
{
    public static final String IFRAME_ID_PREFIX = "easyXDM_embedded-";
    private static final String IFRAME_ID_SUFFIX = "_provider";

    public static String iframeId(String moduleKey)
    {
        return IFRAME_ID_PREFIX + moduleKey + IFRAME_ID_SUFFIX;
    }

    public static String iframeServletPath(String addonKey, String moduleKey)
    {
        return String.format("/plugins/servlet/ac/%s/%s", addonKey, moduleKey);
    }
}
