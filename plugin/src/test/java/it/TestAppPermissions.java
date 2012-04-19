package it;

import com.atlassian.labs.remoteapps.apputils.HttpUtils;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import com.atlassian.labs.remoteapps.test.HtmlDumpRule;
import com.atlassian.labs.remoteapps.test.MessagePage;
import com.atlassian.labs.remoteapps.test.OwnerOfTestedProduct;
import com.atlassian.labs.remoteapps.test.RemoteAppRunner;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestAppPermissions extends AbstractRemoteAppTest
{

    @Test
    public void testNoPermissions() throws Exception
    {
        RemoteAppRunner runner = new RemoteAppRunner(product.getProductInstance().getBaseUrl(),
                "noPermissions")
                .addGeneralPage("page", "Page", "/page", new CallServlet(product.getProductInstance().getBaseUrl()))
                .description("foo")
                .addOAuth()
                .start();

        String status = product.visit(MessagePage.class, "noPermissions", "page")
                .getMessage();
        assertEquals("403", status);
        runner.stop();
    }

    private static class CallServlet extends HttpServlet
    {
        private final String baseUrl;

        public CallServlet(String baseUrl)
        {
            this.baseUrl = baseUrl;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
                ServletException,
                IOException
        {
            int statusCode = HttpUtils.sendFailedSignedGet(new OAuthContext(), baseUrl + "/rest/remoteapptest/latest/user", "betty");
            HttpUtils.renderHtml(resp, "message-page.mu", ImmutableMap.<String, Object>of(
                    "baseurl", baseUrl,
                    "message", String.valueOf(statusCode)
            ));
        }
    }
}
