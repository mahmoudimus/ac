package it;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.test.GeneralPage;
import com.atlassian.plugin.remotable.test.HttpUtils;
import com.atlassian.plugin.remotable.test.MessagePage;
import com.atlassian.plugin.remotable.test.RemotePluginAwarePage;
import com.atlassian.plugin.remotable.test.RemotePluginRunner;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import com.atlassian.plugin.remotable.test.RunnerSignedRequestHandler;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.atlassian.plugin.remotable.test.Utils.createSignedRequestHandler;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAppPermissions extends AbstractRemotablePluginTest
{

    private static final String LICENSE_RESPONSE_STATUS_CODE_ID = "licenseResponseStatusCode";

    @Test
    public void testNoPermissions() throws Exception
    {
        RunnerSignedRequestHandler signedRequestHandler = createSignedRequestHandler("noPermissions");
        RemotePluginRunner runner = new RemotePluginRunner(product.getProductInstance().getBaseUrl(),
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


    @Test
    public void testPluginDoesntHavePermissionToRetrievePluginLicense() throws Exception
    {
        RemotePluginRunner runner = createLicenseRetrievingPlugin().start();

        assertThat(visitLicenseResponsePage().waitForValue(LICENSE_RESPONSE_STATUS_CODE_ID), is("403"));

        runner.stop();
    }

    @Test
    public void testPluginHasPermissionsToRetrievePluginLicense() throws Exception
    {
        RemotePluginRunner runner = createLicenseRetrievingPlugin(Permissions.READ_LICENSE).start();

        assertThat(visitLicenseResponsePage().waitForValue(LICENSE_RESPONSE_STATUS_CODE_ID), not("403"));

        runner.stop();
    }

    private RemotePluginTestPage visitLicenseResponsePage()
    {
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "pluginLicensePage", "Plugin License Page");
        return page.clickRemotePluginLink();
    }

    private RemotePluginRunner createLicenseRetrievingPlugin(String... permissions) throws IOException, NoSuchAlgorithmException
    {
        RunnerSignedRequestHandler signedRequestHandler = createSignedRequestHandler("license-permissions");
        RemotePluginRunner pluginRunner = new RemotePluginRunner(product.getProductInstance().getBaseUrl(), "license-permissions")
                .addGeneralPage("pluginLicensePage", "Plugin License Page", "/pluginLicense",
                        new RetrieveLicenseServlet(product.getProductInstance().getBaseUrl(), signedRequestHandler))
                .description("plugin license retrieve")
                .addOAuth(signedRequestHandler)
                .addPermission(Permissions.CREATE_OAUTH_LINK);
        for (String permission : permissions)
        {
            pluginRunner.addPermission(permission);
        }
        return pluginRunner;
    }

    private static class RetrieveLicenseServlet extends AbstractHttpServlet
    {
        public RetrieveLicenseServlet(String baseUrl, RunnerSignedRequestHandler signedRequestHandler)
        {
            super(baseUrl, signedRequestHandler, "plugin-license.mu");
        }

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException
        {
            int statusCode = sendFailedSignedGet(signedRequestHandler, baseUrl + "/rest/remotable-plugins/latest/license/", "betty");
            HttpUtils.renderHtml(resp, templateName,
                    ImmutableMap.<String, Object>of("baseUrl", baseUrl, LICENSE_RESPONSE_STATUS_CODE_ID, String.valueOf(statusCode)));
        }
    }

    private static class CallServlet extends AbstractHttpServlet
    {
        public CallServlet(String baseUrl, RunnerSignedRequestHandler signedRequestHandler)
        {
            super(baseUrl, signedRequestHandler, "message-page.mu");
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
                ServletException,
                IOException
        {
            int statusCode = sendFailedSignedGet(signedRequestHandler, baseUrl + "/rest/remoteplugintest/latest/user", "betty");
            HttpUtils.renderHtml(resp, templateName,
                    ImmutableMap.<String, Object>of("baseurl", baseUrl, "message", String.valueOf(statusCode)));
        }
    }

    private static abstract class AbstractHttpServlet extends HttpServlet
    {
        protected final String baseUrl;
        protected final RunnerSignedRequestHandler signedRequestHandler;
        protected final String templateName;

        protected AbstractHttpServlet(String baseUrl, RunnerSignedRequestHandler signedRequestHandler, String templateName)
        {
            this.baseUrl = baseUrl;
            this.signedRequestHandler = signedRequestHandler;
            this.templateName = templateName;
            Plugin plugin = mock(Plugin.class);
            when(plugin.getResourceAsStream(templateName)).thenReturn(getClass().getResourceAsStream("/" + templateName));
            PluginRetrievalService pluginRetrievalService = mock(PluginRetrievalService.class);
            when(pluginRetrievalService.getPlugin()).thenReturn(plugin);
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
