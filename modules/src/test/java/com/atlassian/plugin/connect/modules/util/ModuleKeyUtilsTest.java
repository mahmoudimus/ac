package com.atlassian.plugin.connect.modules.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the conversion of passed in strings to valid module keys that the plugin system needs.
 */
public class ModuleKeyUtilsTest
{
    @Test
    public void generatedKey() throws Exception
    {
        String expected = "web-item-";
        String prefix = "web Item";

        String key = ModuleKeyUtils.generateKey(prefix);
        Assert.assertTrue(key.startsWith(expected));
        Assert.assertTrue(StringUtils.isNumeric(StringUtils.substringAfter(key, expected)));
    }
}
