package com.atlassian.labs.remoteapps.test.remoteapp.junit.all;

import com.atlassian.labs.remoteapps.test.RegistrationOnStartListener;
import com.atlassian.labs.remoteapps.test.remoteapp.OAuthContext;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class PermittedRestCallTest
{
    @Test
    public void testCall() throws Exception
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try
        {
            String baseurl = RegistrationOnStartListener.HOST_BASEURL;
            HttpGet get = new HttpGet(baseurl + "/rest/remoteapptest/latest/?user_id=betty");
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            OAuthContext.INSTANCE.sign(get);
            String name = httpclient.execute(get, responseHandler);
            assertEquals("betty", name);
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }
    }
}
