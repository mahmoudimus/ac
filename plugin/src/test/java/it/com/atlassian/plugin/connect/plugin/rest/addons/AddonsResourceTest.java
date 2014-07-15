package it.com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.collect.Lists;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.util.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.test.util.AddonUtil.randomWebItemBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith (AtlassianPluginsTestRunner.class)
public class AddonsResourceTest
{
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ApplicationProperties applicationProperties;

    private static String REST_BASE = "/atlassian-connect/1/addons";

    private Plugin plugin;

    public AddonsResourceTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
            ApplicationProperties applicationProperties)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.applicationProperties = applicationProperties;
    }

    @Before
    public void setUp() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
        plugin = installPlugin();
        System.out.println("Installed add-on " + plugin.getKey());
    }

    @After
    public void tearDown() throws IOException
    {
        if (null != plugin)
        {
            System.out.println("Uninstalling add-on " + plugin.getKey());
            testPluginInstaller.uninstallAddon(plugin);
        }
    }

    @Test
    public void anonymousRequestReturns401() throws IOException
    {
        RequestUtil requestUtil = new RequestUtil(applicationProperties);
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE))
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);
        assertEquals("Anonymous request to addons resource should return 401", 401, response.getStatusCode());
        assertEquals("Anonymous request to addons resource should return 401 in body", 401, getStatusCode(response));
    }

    @Test
    public void nonAdminRequestReturns401() throws IOException
    {
        RequestUtil requestUtil = new RequestUtil(applicationProperties);
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE))
                .setUsername("barney")
                .setPassword("barney")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);
        assertEquals("User request to addons resource should return 401", 401, response.getStatusCode());
        assertEquals("User request to addons resource should return 401 in body", 401, getStatusCode(response));
    }

    @Test
    public void correctAddonsList() throws IOException
    {
        RequestUtil requestUtil = new RequestUtil(applicationProperties);
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);
        assertEquals("Addons resource should return 200", 200, response.getStatusCode());

        List<Map> addons = getAddonListWithoutLucidchart(response);

        assertTrue("Addons resource should return list with add-on", addons.size() > 0);
        assertEquals("Only add-on should be the created one", plugin.getKey(), addons.get(0).get("key"));
    }

    @Test
    public void noXmlAddonsReturned() throws IOException
    {
        RequestUtil requestUtil = new RequestUtil(applicationProperties);
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "?type=xml"))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);
        assertEquals("Addons resource should return 200", 200, response.getStatusCode());

        List<Map> addons = getAddonListWithoutLucidchart(response);

        assertTrue("No xml add-ons should be returned", addons.size() == 0);
    }

    @Test
    public void oneJsonAddonReturned() throws IOException
    {
        RequestUtil requestUtil = new RequestUtil(applicationProperties);
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "?type=json"))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);
        assertEquals("Addons resource should return 200", 200, response.getStatusCode());

        List<Map> addons = getAddonListWithoutLucidchart(response);

        assertTrue("Only one JSON add-on should be returned", addons.size() == 1);
        assertEquals("Only add-on should be the created one", plugin.getKey(), addons.get(0).get("key"));
    }

    @Test
    public void incorrectTypeReturnsError() throws IOException
    {
        RequestUtil requestUtil = new RequestUtil(applicationProperties);
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "?type=yaml"))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addons resource should return 400", 400, response.getStatusCode());
        assertEquals("Error status code should be 400", 400, getStatusCode(response));
        assertTrue("Error status message should exist", StringUtils.isNotBlank(getErrorMessage(response)));
    }

    @Test
    public void getOneAddon() throws IOException
    {
        RequestUtil requestUtil = new RequestUtil(applicationProperties);
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + plugin.getKey()))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addon should be found", 200, response.getStatusCode());
        assertEquals("Addon key is incorrect", plugin.getKey(), response.getJsonBody().get("key"));
        assertEquals("Addon state is incorrect", "ENABLED", response.getJsonBody().get("state"));
        assertEquals("Addon version is incorrect", "1.0", response.getJsonBody().get("version"));
        assertEquals("Addon type is incorrect", "JSON", response.getJsonBody().get("type"));
        assertNotNull("Addon should have applink", response.getJsonBody().get("applink"));
        assertNotNull("Addon should have links", response.getJsonBody().get("links"));
    }

    @Test
    public void uninstallJsonAddon() throws IOException
    {
        RequestUtil requestUtil = new RequestUtil(applicationProperties);
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.DELETE)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + plugin.getKey()))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addon should be deleted", 200, response.getStatusCode());
        assertEquals("Addon key is incorrect", plugin.getKey(), response.getJsonBody().get("key"));
        assertEquals("Addon version is incorrect", "1.0", response.getJsonBody().get("version"));
        assertEquals("Addon type is incorrect", "JSON", response.getJsonBody().get("type"));

        requestUtil = new RequestUtil(applicationProperties);
        request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "?type=json"))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        response = requestUtil.makeRequest(request);
        assertEquals("Addons resource should return 200", 200, response.getStatusCode());

        List<Map> addons = getAddonListWithoutLucidchart(response);

        assertTrue("No JSON add-on should be returned", addons.isEmpty());
    }

//    @Test
//    public void uninstallXmlAddon() throws IOException
//    {
//        RequestUtil requestUtil = new RequestUtil(applicationProperties);
//        RequestUtil.Request request = requestUtil.requestBuilder()
//                .setMethod(HttpMethod.DELETE)
//                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + plugin.getKey()))
//                .setUsername("admin")
//                .setPassword("admin")
//                .build();
//
//        RequestUtil.Response response = requestUtil.makeRequest(request);
//
//        assertEquals("Addon should be deleted", 200, response.getStatusCode());
//        assertEquals("Addon key is incorrect", plugin.getKey(), response.getJsonBody().get("key"));
//        assertEquals("Addon version is incorrect", "1.0", response.getJsonBody().get("version"));
//        assertEquals("Addon type is incorrect", "JSON", response.getJsonBody().get("type"));
//
//        requestUtil = new RequestUtil(applicationProperties);
//        request = requestUtil.requestBuilder()
//                .setMethod(HttpMethod.GET)
//                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "?type=json"))
//                .setUsername("admin")
//                .setPassword("admin")
//                .build();
//
//        response = requestUtil.makeRequest(request);
//        assertEquals("Addons resource should return 200", 200, response.getStatusCode());
//
//        List<Map> addons = getAddonListWithoutLucidchart(response);
//
//        assertTrue("No JSON add-on should be returned", addons.isEmpty());
//    }

    private Plugin installPlugin() throws IOException
    {
        String key = "ac-test-" + System.currentTimeMillis();
        ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
                .withKey(key)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withModule("webItems", randomWebItemBean())
                .build();

        return testPluginInstaller.installAddon(addonBean);
    }

    private int getStatusCode(RequestUtil.Response response)
    {
        return ((Double) response.getJsonBody().get("status-code")).intValue();
    }

    private String getErrorMessage(RequestUtil.Response response)
    {
        return ((String) response.getJsonBody().get("message"));
    }

    @SuppressWarnings ("unchecked")
    private List<Map> getAddonListWithoutLucidchart(RequestUtil.Response response)
    {
        List<Map> allAddons = ((List<Map>) response.getJsonBody().get("addons"));
        List<Map> addons = Lists.newArrayList();
        // workaround lucidchart bundle for the moment
        for (Map addon : allAddons)
        {
            if (!"lucidchart-app".equals(addon.get("key")))
            {
                addons.add(addon);
            }
        }
        return addons;
    }
}
