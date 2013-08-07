package it;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.GeneralPageModule;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestRedirects extends AbstractBrowserlessTest
{
    @BeforeClass
    public static void setupUrlHandlers()
    {
        HttpURLConnection.setFollowRedirects(false);
    }

    @Test
    public void testPermanentRedirect() throws Exception
    {
        AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner(baseUrl, "permanentRedirect")
                .add(GeneralPageModule.key("page")
                                      .name("Page")
                                      .path("/page")
                                      .resource(new MessageServlet()))
                .start();

        URL url = new URL(baseUrl + "/plugins/servlet/redirect/permanent?app_key=permanentRedirect&app_url=/page&message=bar");
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, yc.getResponseCode());

        // follow redirect
        String redirectUrl = yc.getHeaderField("Location");
        HttpURLConnection conn = (HttpURLConnection) new URL(redirectUrl).openConnection();
        String responseText = IOUtils.toString(conn.getInputStream());
        assertEquals("bar", responseText);

        runner.stop();
    }

    @Test
    public void testOAuthRedirect() throws Exception
    {
        AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner(baseUrl)
                .add(GeneralPageModule.key("page")
                                      .name("Page")
                                      .path("/page")
                                      .resource(new MessageServlet()))
                .addOAuth()
                .start();

        URL url = new URL(baseUrl + "/plugins/servlet/redirect/oauth?app_key=" + runner.getPluginKey() + "&app_url=/page&message=bar");
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, yc.getResponseCode());

        // follow redirect
        String redirectUrl = yc.getHeaderField("Location");
        assertTrue(redirectUrl.contains("oauth_signature="));
        HttpURLConnection conn = (HttpURLConnection) new URL(redirectUrl).openConnection();
        String responseText = IOUtils.toString(conn.getInputStream());
        assertEquals("bar", responseText);

        runner.stop();
    }

    private static final class MessageServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            resp.setContentType("text/plain");
            resp.getWriter().write(req.getParameter("message"));
            resp.getWriter().close();
        }
    }
}
