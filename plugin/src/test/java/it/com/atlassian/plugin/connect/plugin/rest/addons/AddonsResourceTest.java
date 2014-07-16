package it.com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.modzdetector.IOUtils;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.connect.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.connect.spi.Filenames;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.collect.Lists;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.installer.XmlOAuthToJsonJwtUpdateTest;
import it.com.atlassian.plugin.connect.util.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
    private static final Logger LOG = LoggerFactory.getLogger(AddonsResourceTest.class);

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final RequestUtil requestUtil;

    private static String REST_BASE = "/atlassian-connect/1/addons";

    private Plugin jsonAddon;
    private Plugin xmlAddon;

    public AddonsResourceTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
            ApplicationProperties applicationProperties)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.requestUtil = new RequestUtil(applicationProperties);
    }

    @BeforeClass
    public void setUp() throws IOException
    {
        Iterable<String> installedAddonKeys = testPluginInstaller.getInstalledAddonKeys();
        if (installedAddonKeys.iterator().hasNext())
        {
            System.out.println("*** INSTALLED ADD-ONS: ");
            for (String key : installedAddonKeys)
            {
                System.out.println("*** " + key);
            }
        }

        testAuthenticator.authenticateUser("admin");
        jsonAddon = installJsonAddon();
        xmlAddon = installXmlAddon();
    }

    @AfterClass
    public void tearDown() throws IOException
    {
        if (null != jsonAddon)
        {
            try
            {
                testPluginInstaller.uninstallJsonAddon(jsonAddon);
            }
            catch (IOException e)
            {
                LOG.error("Could not uninstall json addon", e);
            }
        }
        if (null != xmlAddon)
        {
            try
            {
                testPluginInstaller.uninstallXmlAddon(xmlAddon);
            }
            catch (IOException e)
            {
                LOG.error("Could not uninstall xml addon", e);
            }
        }

        Iterable<String> installedAddonKeys = testPluginInstaller.getInstalledAddonKeys();
        if (installedAddonKeys.iterator().hasNext())
        {
            System.out.println("*** REMAINING ADD-ONS: ");
            for (String key : installedAddonKeys)
            {
                System.out.println("*** " + key);
            }
        }
    }

    @Test
    public void anonymousRequestReturns401() throws IOException
    {
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
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);
        assertEquals("Addons resource should return 200", 200, response.getStatusCode());

        List<Map> addons = getAddonListWithoutLucidchart(response);

        assertEquals("Addons resource should return list with two add-on", 2, addons.size());
    }

    @Test
    public void oneXmlAddonReturned() throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "?type=xml"))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);
        assertEquals("Addons resource should return 200", 200, response.getStatusCode());

        List<Map> addons = getAddonListWithoutLucidchart(response);

        assertEquals("One xml add-on should be returned", 1, addons.size());
        assertEquals("Only add-on should be the created one", xmlAddon.getKey(), addons.get(0).get("key"));
        assertEquals("Add-on type should be XML", "XML", addons.get(0).get("type"));
    }

    @Test
    public void oneJsonAddonReturned() throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "?type=json"))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);
        assertEquals("Addons resource should return 200", 200, response.getStatusCode());

        List<Map> addons = getAddonListWithoutLucidchart(response);

        assertEquals("One json add-on should be returned", 1, addons.size());
        assertEquals("Only add-on should be the created one", jsonAddon.getKey(), addons.get(0).get("key"));
        assertEquals("Add-on type should be JSON", "JSON", addons.get(0).get("type"));
    }

    @Test
    public void incorrectTypeReturnsError() throws IOException
    {
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
    public void testAddonKey() throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + jsonAddon.getKey()))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addon should be found", 200, response.getStatusCode());
        assertEquals("Addon key is incorrect", jsonAddon.getKey(), response.getJsonBody().get("key"));
    }

    @Test
    public void testAddonState() throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + jsonAddon.getKey()))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addon should be found", 200, response.getStatusCode());
        assertEquals("Addon state is incorrect", "ENABLED", response.getJsonBody().get("state"));
    }

    @Test
    public void testAddonVersion() throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + jsonAddon.getKey()))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addon should be found", 200, response.getStatusCode());
        assertEquals("Addon version is incorrect", "1.0", response.getJsonBody().get("version"));
    }

    @Test
    public void testAddonType() throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + jsonAddon.getKey()))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addon should be found", 200, response.getStatusCode());
        assertEquals("Addon type is incorrect", "JSON", response.getJsonBody().get("type"));
    }

    @Test
    public void testAddonApplink() throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + jsonAddon.getKey()))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addon should be found", 200, response.getStatusCode());
        assertNotNull("Addon should have applink", response.getJsonBody().get("applink"));
    }

    @Test
    public void testAddonLinks() throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + jsonAddon.getKey()))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addon should be found", 200, response.getStatusCode());
        assertNotNull("Addon should have links", response.getJsonBody().get("links"));
    }

    @Test
    @Ignore
    public void uninstallJsonAddon() throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.DELETE)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + jsonAddon.getKey()))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addon should be deleted", 200, response.getStatusCode());
        assertEquals("Addon key is incorrect", jsonAddon.getKey(), response.getJsonBody().get("key"));
        assertEquals("Addon version is incorrect", "1.0", response.getJsonBody().get("version"));
        assertEquals("Addon type is incorrect", "JSON", response.getJsonBody().get("type"));

        request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "?type=json"))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        response = requestUtil.makeRequest(request);
        assertEquals("Addons resource should return 200", 200, response.getStatusCode());

        List<Map> addons = getAddonListWithoutLucidchart(response);

        assertEquals("No JSON add-ons should be returned", 0, addons.size());
    }

    @Test
    @Ignore
    public void uninstallXmlAddon() throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.DELETE)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + xmlAddon.getKey()))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addon should be deleted", 200, response.getStatusCode());
        assertEquals("Addon key is incorrect", xmlAddon.getKey(), response.getJsonBody().get("key"));
        assertEquals("Addon version is incorrect", "1", response.getJsonBody().get("version"));
        assertEquals("Addon type is incorrect", "XML", response.getJsonBody().get("type"));

        request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "?type=xml"))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        response = requestUtil.makeRequest(request);
        assertEquals("Addons resource should return 200", 200, response.getStatusCode());

        List<Map> addons = getAddonListWithoutLucidchart(response);

        assertEquals("No XML add-ons should be returned", 0, addons.size());
    }

    private Plugin installJsonAddon() throws IOException
    {
        String key = "ac-test-json-" + System.currentTimeMillis();
        ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
                .withKey(key)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withDescription(getClass().getCanonicalName())
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withModule("webItems", randomWebItemBean())
                .build();

        return testPluginInstaller.installAddon(addonBean);
    }

    @XmlDescriptor
    private Plugin installXmlAddon() throws IOException
    {
        String key = "myaddon_helloworld"; // this is hard coded into xml_oauth_descriptor.xml </meh>
        return testPluginInstaller.installPlugin(createXmlDescriptorFile(key));
    }

    @XmlDescriptor
    private File createXmlDescriptorFile(final String key) throws IOException
    {
        File xmlFile = File.createTempFile(getClass().getSimpleName(), ".xml");
        xmlFile.deleteOnExit();
        return ZipBuilder.buildZip("install-" + key, new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile(Filenames.ATLASSIAN_PLUGIN_XML, getXmlDescriptorContent(key));
            }
        });
    }

    @XmlDescriptor
    private String getXmlDescriptorContent(String key) throws IOException
    {
        String baseUrl = testPluginInstaller.getInternalAddonBaseUrl(key);
        String xml = IOUtils.toString(XmlOAuthToJsonJwtUpdateTest.class.getResourceAsStream("/com/atlassian/connect/xml_oauth_descriptor.xml"))
                .replace("{{localBaseUrl}}", baseUrl)
                .replace("{{user}}", "admin")
                .replace("{{currentTimeMillis}}", String.valueOf(System.currentTimeMillis()));

        String publicKey = "-----BEGIN PUBLIC KEY-----\n" +
        "                MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCuNwJVGkY9XWtuNe7p8PMOEr8O\n" +
        "                WetSAqMxWldFfmNYTbRsI/8ZX/S/5gm4UKZyFUDOICtVddYv1tWW/P31OA5khyQT\n" +
        "                XLp8sYpyNDBuwg00kfmBGleBgcKvePxMAr2y4La1OBz4aE+xK1HJojl2ToAubVY+\n" +
        "                qikVwxXolycVkz8AzQIDAQAB\n" +
        "                -----END PUBLIC KEY-----";

        // preconditions
        {
            String displayUrlText = String.format("display-url=\"%s\"", baseUrl);
            assertTrue(String.format("%s should contain %s", xml, displayUrlText), xml.indexOf(displayUrlText) > 0);

            String publicKeyText = String.format("<public-key>%s</public-key>", publicKey);
            assertTrue(String.format("%s should contain %s", xml, publicKeyText), xml.indexOf(publicKeyText) > 0);

            String pluginKeyText = String.format("key=\"%s\"", key);
            assertTrue(String.format("%s should contain %s", xml, pluginKeyText), xml.indexOf(pluginKeyText) > 0);
        }

        return xml;
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
