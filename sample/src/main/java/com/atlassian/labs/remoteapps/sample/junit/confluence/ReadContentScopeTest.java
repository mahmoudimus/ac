package com.atlassian.labs.remoteapps.sample.junit.confluence;

import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import com.atlassian.labs.remoteapps.apputils.XmlRpcClientFactory;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcStruct;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ReadContentScopeTest
{
    private XmlRpcClientFactory xmlRpcClientFactory = new XmlRpcClientFactory(new OAuthContext());

    @Test
    public void testCall() throws Exception
    {
        try
        {
            XmlRpcClient client = xmlRpcClientFactory.create(Environment.getAllClients().iterator().next(), "betty");
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
