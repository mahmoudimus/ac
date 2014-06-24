package com.atlassian.plugin.connect.test;

import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import org.apache.commons.lang.RandomStringUtils;

import java.util.regex.Pattern;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;

public class AddonTestUtils
{
    public static String randomAddOnKey()
    {
        // include underscores in add-on key: used in the separator at ModuleKeyUtils
        return "com.atlassian.test_addon__" + RandomStringUtils.randomAlphanumeric(8).replaceAll("3", "4").toLowerCase();
    }

    public static String randomModuleKey()
    {
        return RandomStringUtils.randomAlphanumeric(20).replaceAll("3", "4").toLowerCase();
    }

    public static WebItemModuleBean randomWebItemBean()
    {
        return newWebItemBean()
                .withName(new I18nProperty(randomModuleKey(), ""))
                .withKey(randomModuleKey())
                .withLocation("system.nowhere")
                .withUrl("/nowhere")
                .build();
    }

    public static String escapedAddonKey(String addonKey)
    {
        return escapeJQuerySelector(addonKey);
    }

    public static String escapedAddonAndModuleKey(String addonKey, String moduleKey)
    {
        return escapeJQuerySelector(ModuleKeyUtils.addonAndModuleKey(addonKey, moduleKey));
    }

    private static Pattern regex = Pattern.compile("[(!\"#$%&'\\(\\)*+,./:;<=>?@\\[\\\\\\]^`{|}~)]");
    public static String escapeJQuerySelector(String selector)
    {
        if (selector == null)
        {
            return null;
        }
        return regex.matcher(selector).replaceAll("\\\\$0");
    }
}
