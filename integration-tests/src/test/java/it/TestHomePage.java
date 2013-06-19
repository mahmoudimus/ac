package it;

import com.atlassian.plugin.remotable.plugin.loader.universalbinary.UBDispatchFilter;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class TestHomePage extends AbstractBrowserlessTest
{
    @BeforeClass
    public static void setupUrlHandlers()
    {
        HttpURLConnection.setFollowRedirects(false);
    }

    @Test
    public void testDocumentionUrlRedirect() throws InterruptedException, IOException
    {
        URL url = new URL(getBaseUrl());
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        yc.addRequestProperty("Accept", "text/html");
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, yc.getResponseCode());
        assertEquals("http://example.com", yc.getHeaderField("Location"));
    }

    protected String getBaseUrl()
    {
        return baseUrl + UBDispatchFilter.getLocalMountBasePath("app1");
    }

}
