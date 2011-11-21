package com.atlassian.labs.remoteapps.sample.junit.all;

import com.atlassian.labs.remoteapps.sample.HttpServer;
import com.atlassian.labs.remoteapps.sample.OAuthContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class RestCallTest
{
    @Test
    public void testCall() throws Exception
    {
        String result = OAuthContext.INSTANCE.sendSignedGet(HttpServer.getHostBaseUrl() + "/rest/remoteapptest/latest/?user_id=betty");
        assertEquals("betty", result);
    }

    @Test
    public void testUnauthorizedCall() throws Exception
    {
        int status = OAuthContext.INSTANCE.sendFailedSignedGet(
                HttpServer.getHostBaseUrl() + "/rest/remoteappforbidden/latest/?user_id=betty");
        assertEquals(403, status);
    }
}
