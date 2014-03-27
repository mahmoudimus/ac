package it.com.atlassian.plugin.connect.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import it.com.atlassian.plugin.connect.TestAuthenticator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Ensure that Connect supports add-ons updating from OAuth to JWT authentication.
 */
@RunWith(AtlassianPluginsTestRunner.class)
public class OAuthToJwtUpdateWithSaasMigrationTest
{
    private static final Logger LOG = LoggerFactory.getLogger(OAuthToJwtUpdateWithSaasMigrationTest.class);

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ConnectAddonRegistry connectAddonRegistry;
    private Plugin oAuthPlugin;
    private Plugin jwtPlugin;
    private ConnectAddonBean oAuthAddOnBean;

    public OAuthToJwtUpdateWithSaasMigrationTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, ConnectAddonRegistry connectAddonRegistry)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.connectAddonRegistry = connectAddonRegistry;
    }

    @BeforeClass
    public void beforeAllTests() throws IOException
    {
        oAuthAddOnBean = createOAuthAddOnBean();

        //you MUST login as admin before you can use the testPluginInstaler
        testAuthenticator.authenticateUser("admin");
        
        oAuthPlugin = testPluginInstaller.installAddon(oAuthAddOnBean);
        jwtPlugin = testPluginInstaller.installAddon(createJwtAddOn(oAuthAddOnBean));
        oAuthPlugin = null; // we get to this line of code only if installing the update works
    }

    @AfterClass
    public void afterAllTests()
    {
        uninstall(oAuthPlugin);
        uninstall(jwtPlugin);
    }

    private void uninstall(Plugin plugin)
    {
        if (null != plugin)
        {
            try
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
            catch (IOException e)
            {
                LOG.error("Failed to uninstall test plugin " + plugin.getKey() + " during teardown.", e);
            }
        }
    }

    @Test
    public void pluginKeyRemainsTheSame()
    {
        assertEquals(oAuthAddOnBean.getKey(), jwtPlugin.getKey());
    }

    @Test
    public void baseUrlChanges()
    {
        assertFalse(oAuthAddOnBean.getBaseUrl().equals(getNewBaseUrlFromRegistry()));
    }

    @Test
    public void newBaseUrlIsCorrect()
    {
        assertEquals(oAuthAddOnBean.getBaseUrl().replace("oauth-version", "jwt-version"), getNewBaseUrlFromRegistry());
    }

    @Test
    public void baseUrlInRegistryAndInDescriptorAgree()
    {
        assertEquals(getNewBaseUrlFromRegistry(), getNewDescriptor().get("baseUrl").getAsString());
    }

    private String getNewBaseUrlFromRegistry()
    {
        return connectAddonRegistry.getBaseUrl(jwtPlugin.getKey());
    }

    private JsonObject getNewDescriptor()
    {
        return new JsonParser().parse(connectAddonRegistry.getDescriptor(jwtPlugin.getKey())).getAsJsonObject();
    }

    private ConnectAddonBean createJwtAddOn(ConnectAddonBean oAuthAddOn)
    {
        AuthenticationBean authenticationBean = AuthenticationBean.newAuthenticationBean()
                .withType(AuthenticationType.JWT)
                .build();
        return createAddonBean(authenticationBean, oAuthAddOn.getKey(), "/jwt-version");
    }

    private ConnectAddonBean createOAuthAddOnBean()
    {
        AuthenticationBean authenticationBean = AuthenticationBean.newAuthenticationBean()
                .withType(AuthenticationType.OAUTH)
                .withPublicKey("-----BEGIN PUBLIC KEY-----\n" +
                        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArSAz64GtM+Dy+bRHU74B\n" +
                        "dmPSU7h8GuzmivcZ7QSPGNUL9jSH7j+EcDXO0ucHFPNQdKzi7KPHb1WrssTbiJOg\n" +
                        "xJL89stIERKYjE7in/R78rMwAEEEzAG2KTcZR69DtiwM8oQ2da0cuyedVTuB999u\n" +
                        "KD12uei5/yXK0K3qOAV2r2xjgnCKgiYdLKG8CMtxPzQQVkG2lPYXn4sf+AUcUct8\n" +
                        "Y9Y73IPcOnvM1Q7Bl+noulcsrP3WKMIRJs47p5WptPoNj05swkN/k41jLkCkhQWh\n" +
                        "aZu/HN2nuADA9XzWaOsKl3ISuXkb/UvoQKgJBfxIDy3EzHjjgArGNL1g9z53gyt0\n" +
                        "jQIDAQAB\n" +
                        "-----END PUBLIC KEY-----")
                .build();
        String key = getClass().getSimpleName() + "." + System.currentTimeMillis();
        return createAddonBean(authenticationBean, key, "/oauth-version");
    }

    private ConnectAddonBean createAddonBean(AuthenticationBean authenticationBean, String key, String baseUrlSuffix)
    {
        return new ConnectAddonBeanBuilder()
                .withKey(key)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key) + baseUrlSuffix)
                .withAuthentication(authenticationBean)
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .build();
    }
}
