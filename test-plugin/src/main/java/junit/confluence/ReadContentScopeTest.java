package junit.confluence;

//import com.atlassian.confluence.xmlrpc.client.api.ConfluenceSpaceClient;
//import com.atlassian.confluence.xmlrpc.client.api.domain.ExportType;
//import com.atlassian.confluence.xmlrpc.client.api.domain.Space;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcStruct;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.zip.ZipInputStream;

import static junit.ClientKeyRetriever.getClientKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static services.ServiceAccessor.*;

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
                XmlRpcStruct space = getHostXmlRpcClient().invoke("confluence2.getSpace", XmlRpcStruct.class, "", "DS").claim();
                assertEquals("ds", space.getString("key"));
                assertEquals("Demonstration Space", space.getString("name"));
                return null;
            }
        });
    }

    @Test
    public void testSpaceExport() throws Exception
    {
//        getHostHttpClient().callAs(getClientKey(), "betty", new Callable<Void>()
//        {
//            @Override
//            public Void call() throws Exception
//            {
//                InputStream in = getService(ConfluenceSpaceClient.class).exportSpace("DS", ExportType.XML).claim();
//                ZipInputStream zin = new ZipInputStream(in);
//                assertNotNull(zin.getNextEntry());
//                return null;
//            }
//        });
    }

    @Test
    public void testCallWithBinder() throws Exception
    {
//        getHostHttpClient().callAs(getClientKey(), "betty", new Callable<Void>()
//            {
//                @Override
//                public Void call() throws Exception
//                {
//                    ConfluenceSpaceClient service = getService(ConfluenceSpaceClient.class);
//                    Space space = service.getSpace("DS").claim();
//                    assertEquals("ds", space.getKey());
//                    assertEquals("Demonstration Space", space.getName());
//                    return null;
//                }
//            });
    }
}
