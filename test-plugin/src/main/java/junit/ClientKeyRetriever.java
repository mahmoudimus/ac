package junit;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;

import java.io.InputStream;
import java.net.URL;

import static com.atlassian.labs.remoteapps.spi.util.XmlUtils.createSecureSaxReader;

public class ClientKeyRetriever
{
    private static final String clientKey;

    static
    {
        String baseUrl = System.getProperty("baseurl");
        InputStream in = null;
        try
        {
            URL url = new URL(baseUrl + "/plugins/servlet/oauth/consumer-info");
            in = url.openStream();
            Document doc = createSecureSaxReader().read(in);
            clientKey = doc.getRootElement().element("key").getTextTrim();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

    public static String getClientKey()
    {
        return clientKey;
    }
}
