package it.common.iframe;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import it.common.spi.TestedProductAccessor;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;

public class TestRedirects
{
    private final String baseUrl = TestedProductAccessor.get().getTestedProduct().getProductInstance().getBaseUrl();

    @BeforeClass
    public static void setupUrlHandlers()
    {
        HttpURLConnection.setFollowRedirects(false);
    }

    @Test
    public void testPermanentRedirect() throws Exception
    {
        ConnectRunner runner = new ConnectRunner(baseUrl, AddonTestUtils.randomAddOnKey())
                .addModule("generalPages", newPageBean()
                        .withKey("page")
                        .withName(new I18nProperty("Page", null))
                        .withUrl("/page")
                        .build())
                .addRoute("/page", new MessageServlet())
                .setAuthenticationToNone()
                .start();

        try
        {
            URL url = new URL(baseUrl + "/plugins/servlet/redirect/permanent?app_key=" + runner.getAddon().getKey() + "&app_url=/page&message=bar");
            HttpURLConnection yc = (HttpURLConnection) url.openConnection();
            assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, yc.getResponseCode());

            // follow redirect
            String redirectUrl = yc.getHeaderField("Location");
            HttpURLConnection conn = (HttpURLConnection) new URL(redirectUrl).openConnection();
            String responseText = IOUtils.toString(conn.getInputStream());
            assertEquals("bar", responseText);
        }
        finally
        {
            runner.stopAndUninstall();
        }
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
