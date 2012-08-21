package junit.confluence;

import com.atlassian.labs.remoteapps.api.service.http.HostXmlRpcClient;
import services.HostXmlRpcClientAccessor;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcStruct;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ReadContentScopeTest
{
    private final HostXmlRpcClient client;

    public ReadContentScopeTest()
    {
        client = HostXmlRpcClientAccessor.getHostXmlRpcClient();
    }

    @Test
    public void testCall() throws Exception
    {
        try
        {
            XmlRpcStruct space = client.invoke("confluence2.getSpace", XmlRpcStruct.class, "", "DS").claim();
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
