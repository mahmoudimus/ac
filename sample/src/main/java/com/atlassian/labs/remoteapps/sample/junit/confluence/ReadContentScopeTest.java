package com.atlassian.labs.remoteapps.sample.junit.confluence;

import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import com.atlassian.labs.remoteapps.sample.junit.XmlRpcClient;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcStruct;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ReadContentScopeTest
{
    private OAuthContext oAuthContext = new OAuthContext();
    private final String hostBaseUrl = oAuthContext.getHostBaseUrl(Environment.getAllClients().iterator().next());

    @Test
    public void testCall() throws Exception
    {
        try
        {
            final String url = hostBaseUrl + "/rpc/xmlrpc";
            XmlRpcClient client = new XmlRpcClient(url + "?user_id=betty", false);
            client.setRequestProperty("Authorization", oAuthContext.getAuthorizationHeaderValue(url, "POST", "betty"));
            XmlRpcStruct space = (XmlRpcStruct) client.invoke( "confluence2.getSpace", new Object[] { "", "DS" } );
            assertEquals("ds", space.getString("key"));
            assertEquals("Demonstration Space", space.getString("name"));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }
}
