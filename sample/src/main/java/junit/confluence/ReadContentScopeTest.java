package junit.confluence;

import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import com.atlassian.labs.remoteapps.apputils.XmlRpcClientFactory;
import junit.OAuthContextAccessor;
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
        OAuthContext oAuthContext = OAuthContextAccessor.getOAuthContext();
        xmlRpcClientFactory = new XmlRpcClientFactory(oAuthContext);
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
