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
    public void cleanKey() throws Exception
    {
        String expected = "web-item-some-feature";
        String test = "webItem some Feature";

        Assert.assertEquals(expected, ModuleKeyUtils.cleanKey(test));
    }

    @Test
    public void generatedKey() throws Exception
    {
        String expected = "web-item-";
        String prefix = "web Item";

        String key = ModuleKeyUtils.generateKey(prefix);
        Assert.assertTrue(key.startsWith(expected));
        Assert.assertTrue(StringUtils.isNumeric(StringUtils.substringAfter(key, expected)));
    }

    @Test
    public void lowerCamel() throws Exception
    {
        String expected = "some-feature";
        String test = "someFeature";

        Assert.assertEquals(expected, ModuleKeyUtils.camelCaseOrSpaceToDashed(test));

    }

    @Test
    public void upperCamel() throws Exception
    {
        String expected = "some-feature";
        String test = "SomeFeature";

        Assert.assertEquals(expected, ModuleKeyUtils.camelCaseOrSpaceToDashed(test));

    }

    @Test
    public void upperSpaced() throws Exception
    {
        String expected = "some-feature";
        String test = "Some Feature";

        Assert.assertEquals(expected, ModuleKeyUtils.camelCaseOrSpaceToDashed(test));

    }

    @Test
    public void lowerSpaced() throws Exception
    {
        String expected = "some-feature";
        String test = "some Feature";

        Assert.assertEquals(expected, ModuleKeyUtils.camelCaseOrSpaceToDashed(test));

    }

    @Test
    public void acronymCamel() throws Exception
    {
        String expected = "some-feature";
        String test = "SOMEFeature";

        Assert.assertEquals(expected, ModuleKeyUtils.camelCaseOrSpaceToDashed(test));

    }

    @Test
    public void issueCamel() throws Exception
    {
        String expected = "acdev-1286-some-feature";
        String test = "ACDEV-1286-some-feature";

        Assert.assertEquals(expected, ModuleKeyUtils.camelCaseOrSpaceToDashed(test));

    }

    @Test
    public void alreadyLower() throws Exception
    {
        String expected = "google-link";
        String test = "google link";

        Assert.assertEquals(expected, ModuleKeyUtils.cleanKey(test));
    }

    @Test
    public void specialCharacters() throws Exception
    {
        String expected = "hayao-miyazaki------";
        String test = "Hayao Miyazaki (宮崎駿)";

        Assert.assertEquals(expected, ModuleKeyUtils.cleanKey(test));

        Assert.assertEquals(expected, ModuleKeyUtils.cleanKey(test));
    }
}
