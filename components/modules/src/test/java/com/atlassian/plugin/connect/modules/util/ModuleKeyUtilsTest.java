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
    public void addonAndModuleKey() throws Exception
    {
        String addonKey = "plugin";
        String moduleKey = "module";

        Assert.assertEquals("Namespaced key is generated correctly", "plugin__module", ModuleKeyUtils.addonAndModuleKey(addonKey, moduleKey));
    }

    @Test
    public void addonKeyWithDotsAndModuleKey() throws Exception
    {
        String addonKey = "com.test.plugin";
        String moduleKey = "module";

        Assert.assertEquals("Namespaced key is generated correctly with dots", "com.test.plugin__module", ModuleKeyUtils.addonAndModuleKey(addonKey, moduleKey));
    }

    @Test
    public void addonAndModuleKeyWithAllTheThings() throws Exception
    {
        String addonKey = "com.test.my_plugin";
        String moduleKey = "module_thing1";

        Assert.assertEquals("Module key is extracted from key correctly", "com.test.my_plugin__module_thing1", ModuleKeyUtils.addonAndModuleKey(addonKey, moduleKey));
    }

    @Test
    public void extractAddonKeyWithAllTheThings() throws Exception
    {
        String addonKey = "com.test.my_plugin";
        String moduleKey = "module_thing1";

        String key = ModuleKeyUtils.addonAndModuleKey(addonKey, moduleKey);

        Assert.assertEquals("Addon key is extracted from key correctly", addonKey, ModuleKeyUtils.addonKeyOnly(key));
    }

    @Test
    public void extractAddonKeyWithTwoUnderscores() throws Exception
    {
        String addonKey = "com.test__plugin.my__plugin";
        String moduleKey = "module_thing1";

        String key = ModuleKeyUtils.addonAndModuleKey(addonKey, moduleKey);

        Assert.assertEquals("Addon key with underscores is extracted from key correctly", addonKey, ModuleKeyUtils.addonKeyOnly(key));
    }

    @Test
    public void extractModuleKeyWithAllTheThings() throws Exception
    {
        String addonKey = "com.test.my_plugin";
        String moduleKey = "module_thing1";

        String key = ModuleKeyUtils.addonAndModuleKey(addonKey, moduleKey);

        Assert.assertEquals("Module key is extracted from key correctly", moduleKey, ModuleKeyUtils.moduleKeyOnly(key));
    }

    @Test
    public void toCompleteKeyWithAllTheThings() throws Exception
    {
        String addonKey = "com.test.my_plugin";
        String moduleKey = "module_thing1";

        String key = ModuleKeyUtils.addonAndModuleKey(addonKey, moduleKey);

        String completeKey = addonKey + ":" + moduleKey;

        Assert.assertEquals("complete key is generated from key correctly", completeKey, ModuleKeyUtils.toCompleteKey(key));
    }
}
