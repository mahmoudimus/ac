package it;

import com.atlassian.labs.remoteapps.apputils.HttpUtils;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import com.atlassian.labs.remoteapps.test.MessagePage;
import com.atlassian.labs.remoteapps.test.RemoteAppRunner;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.atlassian.labs.remoteapps.test.Utils.createOAuthContext;
import static org.junit.Assert.assertEquals;

public class TestAppPermissions extends AbstractRemoteAppTest
{

    @Test
    public void testNoPermissions() throws Exception
    {
        OAuthContext oAuthContext = createOAuthContext("noPermissions");
        RemoteAppRunner runner = new RemoteAppRunner(product.getProductInstance().getBaseUrl(),
                "noPermissions")
                .addGeneralPage("page", "Page", "/page", new CallServlet(product.getProductInstance().getBaseUrl(), oAuthContext))
                .description("foo")
                .addOAuth(oAuthContext)
                .start();

        String status = product.visit(MessagePage.class, "noPermissions", "page")
                .getMessage();
        assertEquals("403", status);
        runner.stop();
    }

    private static class CallServlet extends HttpServlet
    {
        private final String baseUrl;
        private final OAuthContext oAuthContext;

        public CallServlet(String baseUrl, OAuthContext oAuthContext)
        {
            this.baseUrl = baseUrl;
            this.oAuthContext = oAuthContext;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
                ServletException,
                IOException
        {
            int statusCode = HttpUtils.sendFailedSignedGet(oAuthContext, baseUrl + "/rest/remoteapptest/latest/user", "betty");
            HttpUtils.renderHtml(resp, "message-page.mu", ImmutableMap.<String, Object>of(
                    "baseurl", baseUrl,
                    "message", String.valueOf(statusCode)
            ));
        }
    }
}
