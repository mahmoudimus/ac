package it;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.service.SignedRequestHandler;
import com.atlassian.plugin.connect.spi.Permissions;
import com.atlassian.plugin.connect.test.HttpUtils;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.MessagePage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.GeneralPageModule;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.common.collect.ImmutableMap;

import org.junit.After;
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

import static it.TestConstants.BETTY_USERNAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAppPermissions extends ConnectWebDriverTestBase
{
    public static final String EXTRA_PREFIX = "servlet-";
    private static final String LICENSE_RESPONSE_STATUS_CODE_ID = "licenseResponseStatusCode";

    private AtlassianConnectAddOnRunner runner;
    
    @After
    public void killRunner() throws Exception
    {
        if(null != runner)
        {
            runner.stopAndUninstall();
        }
    }
    
    @Test
    public void testNoPermissions() throws Exception
    {
        runner = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .description("foo")
                .addOAuth();

        runner.add(GeneralPageModule.key("page")
                                    .name("Page")
                                    .path("/page")
                                    .resource(new CallServlet(product.getProductInstance().getBaseUrl(), runner.getSignedRequestHandler().get())))
              .start();

        String status = product.visit(MessagePage.class, runner.getPluginKey(), "page", EXTRA_PREFIX).getMessage();
        assertEquals("403", status);
    }


    @Test
    public void testPluginDoesntHavePermissionToRetrievePluginLicense() throws Exception
    {
        runner = createLicenseRetrievingPlugin().start();

        assertThat(visitLicenseResponsePage().waitForValue(LICENSE_RESPONSE_STATUS_CODE_ID), is("403"));

    }

    @Test
    public void testPluginHasPermissionsToRetrievePluginLicense() throws Exception
    {
        runner = createLicenseRetrievingPlugin(Permissions.READ_LICENSE).start();

        assertThat(visitLicenseResponsePage().waitForValue(LICENSE_RESPONSE_STATUS_CODE_ID), not("403"));

    }

    private RemotePluginTestPage visitLicenseResponsePage()
    {
        logout();
        loginAsBetty();
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "pluginLicensePage", "Plugin License Page", EXTRA_PREFIX);
        return page.clickRemotePluginLink();
    }

    private AtlassianConnectAddOnRunner createLicenseRetrievingPlugin(String... permissions) throws IOException, NoSuchAlgorithmException
    {
        AtlassianConnectAddOnRunner pluginRunner = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .description("plugin license retrieve")
                .addOAuth();

        pluginRunner.add(GeneralPageModule.key("pluginLicensePage")
                                          .name("Plugin License Page")
                                          .path("/pluginLicense")
                                          .resource(new RetrieveLicenseServlet(product.getProductInstance().getBaseUrl(), pluginRunner.getSignedRequestHandler().get())));
        for (String permission : permissions)
        {
            pluginRunner.addPermission(permission);
        }
        return pluginRunner;
    }

    private static class RetrieveLicenseServlet extends AbstractHttpServlet
    {
        public RetrieveLicenseServlet(String baseUrl, SignedRequestHandler signedRequestHandler)
        {
            super(baseUrl, signedRequestHandler, "plugin-license.mu");
        }

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException
        {
            int statusCode = sendFailedSignedGet(signedRequestHandler, baseUrl + "/rest/atlassian-connect/latest/license/", "betty");
            HttpUtils.renderHtml(resp, templateName,
                    ImmutableMap.<String, Object>of("baseUrl", baseUrl, LICENSE_RESPONSE_STATUS_CODE_ID, String.valueOf(statusCode)));
        }
    }

    private static class CallServlet extends AbstractHttpServlet
    {
        public CallServlet(String baseUrl, SignedRequestHandler signedRequestHandler)
        {
            super(baseUrl, signedRequestHandler, "message-page.mu");
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
                ServletException,
                IOException
        {
            int statusCode = sendFailedSignedGet(signedRequestHandler, baseUrl + "/rest/remoteplugintest/latest/user", BETTY_USERNAME);
            HttpUtils.renderHtml(resp, templateName,
                    ImmutableMap.<String, Object>of("baseurl", baseUrl, "message", String.valueOf(statusCode)));
        }
    }

    private static abstract class AbstractHttpServlet extends HttpServlet
    {
        protected final String baseUrl;
        protected final SignedRequestHandler signedRequestHandler;
        protected final String templateName;

        protected AbstractHttpServlet(String baseUrl, SignedRequestHandler signedRequestHandler, String templateName)
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
