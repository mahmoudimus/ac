package com.atlassian.labs.remoteapps.sample.junit.confluence;

import com.atlassian.labs.remoteapps.sample.OAuthContext;
import com.atlassian.labs.remoteapps.sample.junit.XmlRpcClient;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcStruct;

import static com.atlassian.labs.remoteapps.sample.HttpServer.getHostBaseUrl;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ReadContentScopeTest
{
    @Test
    public void testCall() throws Exception
    {
        final String url = getHostBaseUrl() + "/rpc/xmlrpc?user_id=betty";
        XmlRpcClient client = new XmlRpcClient(url, false);
        OAuthContext.INSTANCE.sign(url, client);
        XmlRpcStruct space = (XmlRpcStruct) client.invoke( "confluence2.getSpace", new Object[] { "", "DS" } );
        assertEquals("ds", space.getString("key"));
        assertEquals("Demonstration Space", space.getString("name"));

    }
}
