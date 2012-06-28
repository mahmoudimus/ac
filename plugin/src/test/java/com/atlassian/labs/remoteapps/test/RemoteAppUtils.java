package com.atlassian.labs.remoteapps.test;

import com.atlassian.pageobjects.ProductInstance;

import java.io.IOException;
import java.net.URLEncoder;

import static com.atlassian.labs.remoteapps.test.Utils.emptyGet;

/**
 *
 */
public class RemoteAppUtils
{
    public static void clearMacroCaches(ProductInstance productInstance, String appKey) throws IOException
    {
        emptyGet(productInstance.getBaseUrl() + "/app/"+appKey+"/macroReset?baseurl=" +
                URLEncoder.encode(productInstance.getBaseUrl(), "UTF-8"));
    }
}
