package com.atlassian.labs.remoteapps.test;

import com.atlassian.pageobjects.ProductInstance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.atlassian.labs.remoteapps.test.Utils.emptyGet;
import static com.atlassian.labs.remoteapps.test.Utils.getJson;

/**
 *
 */
public class RemoteAppUtils
{
    public static JSONObject waitForEvent(ProductInstance productInstance, String id) throws IOException, JSONException, InterruptedException
    {
        long expiry = System.currentTimeMillis() + 5 * 1000;

        while (expiry > System.currentTimeMillis())
        {
            JSONArray events = new JSONArray(getJson(productInstance.getBaseUrl() + "/plugins/servlet/app1/proxy/webHook/"));
            if (events.length() > 0)
            {
                for (int i = events.length() -1; i >= 0; i--)
                {
                    org.json.JSONObject event = events.getJSONObject(i);
                    if (id.equals(event.getString("event")))
                    {
                        return event.getJSONObject("body");
                    }
                }
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Event '" + id + "' not published");
    }

    public static void clearMacroCaches(ProductInstance productInstance, String appKey) throws IOException
    {
        emptyGet(productInstance.getBaseUrl() + "/plugins/servlet/"+appKey+"/proxy/macroReset");
    }
}
