package it.com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.util.RequestUtil;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static com.atlassian.plugin.connect.test.util.AddonUtil.randomWebItemBean;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
public class AddonPropertiesResourceTest
{
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final RequestUtil requestUtil;

    private Plugin jsonAddon;
    private String JIRA_ADDRESS = "http://localhost:2990/jira";
    private static String REST_BASE = "/atlassian-connect/1/addons";

    private final String ADDON_KEY = "ac-addon-property-test";

    private final String PROPERTY_REST_ADDR = JIRA_ADDRESS + REST_BASE + "/" + ADDON_KEY + "/properties/";

    @Autowired
    public AddonPropertiesResourceTest(final TestPluginInstaller testPluginInstaller, final TestAuthenticator testAuthenticator, final RequestUtil requestUtil)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.requestUtil = requestUtil;
    }

    @Before
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
    }

    @After
    public void tearDown() throws IOException
    {
        if (null != jsonAddon)
        {
            try
            {
                testPluginInstaller.uninstallAddon(jsonAddon);
            }
            catch (IOException e)
            {
                //LOG.error("Could not uninstall json addon", e);
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
    public void testAddPropertyAndGet() throws Exception
    {
        String propertyKey = "testKey";
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.POST)
                .setUri(requestUtil.getApplicationRestUrl(PROPERTY_REST_ADDR + propertyKey))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        requestUtil.makeRequest(request);

        RequestUtil.Request request2 = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(PROPERTY_REST_ADDR + propertyKey))
                .setUsername("admin")
                .setPassword("admin")
                .build();
        requestUtil.makeRequest(request2);

    }

    private Plugin installJsonAddon() throws IOException
    {
        ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
                .withKey(ADDON_KEY)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(ADDON_KEY))
                .withDescription(getClass().getCanonicalName())
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withModule("webItems", randomWebItemBean())
                .build();

        return testPluginInstaller.installAddon(addonBean);
    }
}
