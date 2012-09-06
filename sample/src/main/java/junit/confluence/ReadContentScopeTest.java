package junit.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceSpaceClient;
import com.atlassian.labs.remoteapps.api.service.confluence.Space;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcStruct;

import java.util.concurrent.Callable;

import static junit.ClientKeyRetriever.getClientKey;
import static org.junit.Assert.assertEquals;
import static services.ServiceAccessor.getHostHttpClient;
import static services.ServiceAccessor.getHostXmlRpcClient;
import static services.ServiceAccessor.getService;

/**
 *
 */
public class ReadContentScopeTest
{
    @Test
    public void testCall() throws Exception
    {
        getHostHttpClient().callAs(getClientKey(), "betty", new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                XmlRpcStruct space = getHostXmlRpcClient().invoke("confluence2.getSpace",
                        XmlRpcStruct.class, "", "DS").claim();
                assertEquals("ds", space.getString("key"));
                assertEquals("Demonstration Space", space.getString("name"));
                return null;
            }
        });
    }

    @Test
    public void testCallWithBinder() throws Exception
    {
        getHostHttpClient().callAs(getClientKey(), "betty", new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    ConfluenceSpaceClient service = getService(ConfluenceSpaceClient.class);
                    Space space = service.getSpace("DS").claim();
                    assertEquals("ds", space.getKey());
                    assertEquals("Demonstration Space", space.getName());
                    return null;
                }
            });
    }
}
