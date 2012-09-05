package it;

import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;
import com.atlassian.labs.remoteapps.spi.Permissions;
import com.atlassian.labs.remoteapps.test.HttpUtils;
import com.atlassian.labs.remoteapps.test.MessagePage;
import com.atlassian.labs.remoteapps.test.RemoteAppRunner;
import com.atlassian.labs.remoteapps.test.RunnerSignedRequestHandler;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static com.atlassian.labs.remoteapps.test.Utils.createSignedRequestHandler;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAppPermissions extends AbstractRemoteAppTest
{

    @Test
    public void testNoPermissions() throws Exception
    {
        RunnerSignedRequestHandler signedRequestHandler = createSignedRequestHandler("noPermissions");
        RemoteAppRunner runner = new RemoteAppRunner(product.getProductInstance().getBaseUrl(),
                "noPermissions")
                .addGeneralPage("page", "Page", "/page",
                        new CallServlet(product.getProductInstance().getBaseUrl(), signedRequestHandler))
                .description("foo")
                .addOAuth(signedRequestHandler)
                .addPermission(Permissions.CREATE_OAUTH_LINK)
                .start();

        String status = product.visit(MessagePage.class, "noPermissions", "page")
                .getMessage();
        assertEquals("403", status);
        runner.stop();
    }

    private static class CallServlet extends HttpServlet
    {
        private final String baseUrl;
        private final HttpUtils httpUtils;
        private final RunnerSignedRequestHandler signedRequestHandler;

        public CallServlet(String baseUrl, RunnerSignedRequestHandler signedRequestHandler)
        {
            this.signedRequestHandler = signedRequestHandler;
            this.baseUrl = baseUrl;
            Plugin plugin = mock(Plugin.class);
            when(plugin.getResourceAsStream("message-page.mu")).thenReturn(getClass().getResourceAsStream("/message-page.mu"));
            PluginRetrievalService pluginRetrievalService = mock(PluginRetrievalService.class);
            when(pluginRetrievalService.getPlugin()).thenReturn(plugin);
            this.httpUtils = new HttpUtils();
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
                ServletException,
                IOException
        {
            int statusCode = sendFailedSignedGet(signedRequestHandler, baseUrl + "/rest/remoteapptest/latest/user", "betty");
            httpUtils.renderHtml(resp, "message-page.mu",
                    ImmutableMap.<String, Object>of("baseurl", baseUrl, "message", String.valueOf(statusCode)));
        }
    }

    private static int sendFailedSignedGet(SignedRequestHandler signedRequestHandler, String uri, String user)
    {
        HttpURLConnection yc = null;
        try
        {
            URL url = new URL(uri + "?user_id=" + user);
            yc = (HttpURLConnection) url.openConnection();
            signedRequestHandler.sign(URI.create(uri), "GET", user, yc);
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    yc.getInputStream()));
            return yc.getResponseCode();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            try
            {
                if (yc != null)
                {
                    return yc.getResponseCode();
                }
                throw new RuntimeException("no status code");
            }
            catch (IOException e1)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
