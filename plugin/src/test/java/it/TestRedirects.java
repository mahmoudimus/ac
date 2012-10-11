package it;

import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.test.RemotePluginRunner;
import com.atlassian.plugin.remotable.test.RunnerSignedRequestHandler;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import static com.atlassian.plugin.remotable.test.Utils.createSignedRequestHandler;
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
	public void testPermanentRedirect() throws Exception, InterruptedException,
            NoSuchAlgorithmException
    {
        RemotePluginRunner runner = new RemotePluginRunner(baseUrl,
                "permanentRedirect")
                .addGeneralPage("page", "Page", "/page", new MessageServlet())
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
    public void testOAuthRedirect() throws Exception, InterruptedException,
            NoSuchAlgorithmException
    {
        RunnerSignedRequestHandler signedRequestHandler = createSignedRequestHandler("oauthRedirect");
        RemotePluginRunner runner = new RemotePluginRunner(baseUrl,
                "oauthRedirect")
                .addGeneralPage("page", "Page", "/page", new MessageServlet())
                .addOAuth(signedRequestHandler)
                .addPermission(Permissions.CREATE_OAUTH_LINK)
                .start();

        URL url = new URL(baseUrl + "/plugins/servlet/redirect/oauth?app_key=oauthRedirect&app_url=/page&message=bar");
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
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
                ServletException,
                IOException
        {
            resp.setContentType("text/plain");
            resp.getWriter().write(req.getParameter("message"));
            resp.getWriter().close();
        }
    }
}
