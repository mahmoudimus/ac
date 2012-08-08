package junit.confluence;

import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.kit.common.XmlRpcClientFactory;
import junit.SignedRequestHandlerAccessor;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcStruct;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ReadContentScopeTest
{
    private final XmlRpcClientFactory xmlRpcClientFactory;

    public ReadContentScopeTest()
    {
        SignedRequestHandler signedRequestHandler = SignedRequestHandlerAccessor.getSignedRequestHandler();
        xmlRpcClientFactory = new XmlRpcClientFactory(signedRequestHandler);
    }

    @Test
    public void testCall() throws Exception
    {
        try
        {
            XmlRpcClient client = xmlRpcClientFactory.create(System.getProperty("baseurl"), "betty");
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
