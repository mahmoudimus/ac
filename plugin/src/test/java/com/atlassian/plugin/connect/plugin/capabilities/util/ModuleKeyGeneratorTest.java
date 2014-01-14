package com.atlassian.plugin.connect.plugin.capabilities.util;

import com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the conversion of passed in strings to valid module keys that the plugin system needs.
 */
public class ModuleKeyGeneratorTest
{
    @Test
    public void nameToKey() throws Exception
    {
        String expected = "some-feature";
        String test = "some Feature";

        assertEquals(expected, ModuleKeyGenerator.nameToKey(test));
    }

    @Test
    public void prefixedNameToKey() throws Exception
    {
        String expected = "web-item-some-feature";
        String prefix = "webItem";
        String test = "some Feature";

        assertEquals(expected, ModuleKeyGenerator.nameToKey(prefix,test));
    }

    @Test
    public void prefixedNameToKeyCamel() throws Exception
    {
        String expected = "webitem-my-web-item";
        String prefix = "webitem";
        String test = "My Web Item";

        assertEquals(expected, ModuleKeyGenerator.nameToKey(prefix,test));
    }

    @Test
    public void generatedKey() throws Exception
    {
        String expected = "web-item-";
        String prefix = "web Item";

        String key = ModuleKeyGenerator.generateKey(prefix);
        assertTrue(key.startsWith(expected));
        assertTrue(StringUtils.isNumeric(StringUtils.substringAfter(key,expected)));
    }

    @Test
    public void lowerCamel() throws Exception
    {
        String expected = "some-feature";
        String test = "someFeature";

        assertEquals(expected, ModuleKeyGenerator.camelCaseOrSpaceToDashed(test));

    }

    @Test
    public void upperCamel() throws Exception
    {
        String expected = "some-feature";
        String test = "SomeFeature";

        assertEquals(expected, ModuleKeyGenerator.camelCaseOrSpaceToDashed(test));

    }

    @Test
    public void upperSpaced() throws Exception
    {
        String expected = "some-feature";
        String test = "Some Feature";

        assertEquals(expected, ModuleKeyGenerator.camelCaseOrSpaceToDashed(test));

    }

    @Test
    public void lowerSpaced() throws Exception
    {
        String expected = "some-feature";
        String test = "some Feature";

        assertEquals(expected, ModuleKeyGenerator.camelCaseOrSpaceToDashed(test));

    }

    @Test
    public void acronymCamel() throws Exception
    {
        String expected = "some-feature";
        String test = "SOMEFeature";

        assertEquals(expected, ModuleKeyGenerator.camelCaseOrSpaceToDashed(test));

    }

    @Test
    public void issueCamel() throws Exception
    {
        String expected = "acdev-1286-some-feature";
        String test = "ACDEV-1286-some-feature";

        assertEquals(expected, ModuleKeyGenerator.camelCaseOrSpaceToDashed(test));

    }

    @Test
    public void alreadyLower() throws Exception
    {
        String expected = "google-link";
        String test = "google link";

        assertEquals(expected, ModuleKeyGenerator.nameToKey(test));
    }
}
