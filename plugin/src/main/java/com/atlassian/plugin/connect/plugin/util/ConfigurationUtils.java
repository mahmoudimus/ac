package com.atlassian.plugin.connect.plugin.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationUtils
{
    private static final Logger log = LoggerFactory.getLogger(ConfigurationUtils.class);

    public static int getIntSystemProperty(String name, final int defaultValue)
    {
        try
        {
            return Integer.parseInt(StringUtils.strip(System.getProperty(name)));
        }
        catch (Exception e)
        {
            log.warn(String.format("Failed to retrieve system property '%s' as int due to exception. Returning default value %d.", name, defaultValue), e);
            return defaultValue;
        }
    }
}
