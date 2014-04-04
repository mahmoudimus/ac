package com.atlassian.plugin.connect.test.util;

import org.apache.commons.lang.RandomStringUtils;

public class AddonUtil
{
    public static String randomPluginKey()
    {
        return RandomStringUtils.randomAlphanumeric(20).replaceAll("3", "4").toLowerCase();
    }
}
