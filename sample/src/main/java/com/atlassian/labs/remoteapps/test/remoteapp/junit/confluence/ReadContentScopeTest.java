package com.atlassian.labs.remoteapps.test.remoteapp.junit.confluence;

import com.atlassian.labs.remoteapps.test.RegistrationOnStartListener;
import com.atlassian.labs.remoteapps.test.remoteapp.OAuthContext;
import com.atlassian.labs.remoteapps.test.remoteapp.junit.XmlRpcClient;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcStruct;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ReadContentScopeTest
{
    @Test
    public void testCall() throws Exception
    {
        final String url = RegistrationOnStartListener.HOST_BASEURL + "/rpc/xmlrpc?user_id=betty";
        XmlRpcClient client = new XmlRpcClient(url, false);
        OAuthContext.INSTANCE.sign(url, client);
        XmlRpcStruct space = (XmlRpcStruct) client.invoke( "confluence2.getSpace", new Object[] { "", "DS" } );
        assertEquals("ds", space.getString("key"));
        assertEquals("Demonstration Space", space.getString("name"));

    }
}
