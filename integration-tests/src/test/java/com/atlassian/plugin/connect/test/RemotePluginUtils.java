package com.atlassian.plugin.connect.test;

import java.io.IOException;
import java.net.URLEncoder;

import com.atlassian.pageobjects.ProductInstance;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

import org.apache.commons.lang.RandomStringUtils;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.test.Utils.emptyGet;

/**
 *
 */
public class RemotePluginUtils
{
    public static void clearMacroCaches(ProductInstance productInstance, String appKey) throws IOException
    {

        emptyGet(productInstance.getBaseUrl() + "/app/" + appKey + "/macroReset?baseurl=" +
                URLEncoder.encode(productInstance.getBaseUrl(), "UTF-8"));
    }
    
    public static String randomPluginKey()
    {
        return RandomStringUtils.randomAlphanumeric(20).replaceAll("3", "4").toLowerCase();
    }

    public static WebItemModuleBean randomWebItemBean()
    {
        return newWebItemBean().withName(new I18nProperty(randomPluginKey(), "")).withKey(randomPluginKey()).withLocation("system.nowhere").withUrl("/nowhere").build();
    }
}
