package com.atlassian.plugin.connect.core.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationUtils
{
    private static final Logger log = LoggerFactory.getLogger(ConfigurationUtils.class);

    public static int getIntSystemProperty(String name, final int defaultValue)
    {
        int result = defaultValue;
        String rawPropertyValue = System.getProperty(name);

        if (null == rawPropertyValue)
        {
            log.warn(String.format("System property '%s' does not exist or has a null value. Returning default value %d.", name, defaultValue));
        }
        else
        {
            try
            {
                result = Integer.parseInt(StringUtils.strip(rawPropertyValue));
            }
            catch (Exception e)
            {
                log.warn(String.format("Failed to retrieve system property '%s' as int due to exception. Returning default value %d.", name, defaultValue), e);
            }
        }

        return result;
    }
}
