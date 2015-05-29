package com.atlassian.plugin.connect.testsupport.util;

import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import org.apache.commons.lang.RandomStringUtils;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;

public class AddonUtil
{
    public static String randomPluginKey()
    {
        return RandomStringUtils.randomAlphanumeric(20).replaceAll("3", "4").toLowerCase();
    }
    
    public static WebItemModuleBean randomWebItemBean()
    {
        return newWebItemBean().withName(new I18nProperty(randomPluginKey(), "")).withKey(randomPluginKey()).withLocation("system.nowhere").withUrl("/nowhere").build();
    }
}
