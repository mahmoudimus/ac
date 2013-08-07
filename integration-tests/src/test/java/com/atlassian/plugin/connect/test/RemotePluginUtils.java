package com.atlassian.plugin.connect.test;

import java.io.IOException;
import java.net.URLEncoder;

import com.atlassian.pageobjects.ProductInstance;

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
}
