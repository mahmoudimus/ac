package it.com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil;
import com.atlassian.plugin.connect.spi.AuthenticationMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnapshot;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.atlassian.plugin.connect.util.auth.TestAuthenticator;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.atlassian.plugin.connect.util.AddonUtil.randomWebItemBean;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Ensure that Connect supports add-ons updating from OAuth to JWT authentication.
 */
@RunWith(AtlassianPluginsTestRunner.class)
public class OAuthToJwtUpdateTest
{
    private static final Logger LOG = LoggerFactory.getLogger(OAuthToJwtUpdateTest.class);

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final AddonTestFilterResults testFilterResults;
    private final ConnectApplinkManager connectApplinkManager;
    private Plugin oAuthPlugin;
    private Plugin jwtPlugin;
    private ConnectAddonBean oAuthAddOnBean;

    public OAuthToJwtUpdateTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
                                ConnectAddonRegistry connectAddonRegistry, AddonTestFilterResults testFilterResults,
                                ConnectApplinkManager connectApplinkManager)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.connectAddonRegistry = connectAddonRegistry;
        this.testFilterResults = testFilterResults;
        this.connectApplinkManager = connectApplinkManager;
    }

    @BeforeClass
    public void beforeAllTests() throws IOException {
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
    public void baseUrlRemainsTheSame()
    {
        assertEquals(oAuthAddOnBean.getBaseUrl(), connectAddonRegistry.getBaseUrl(jwtPlugin.getKey()));
    }

    @Test
    public void baseUrlRemainsTheSameInDescriptor()
    {
        assertEquals(oAuthAddOnBean.getBaseUrl(), getDescriptor().get("baseUrl").getAsString());
    }

    @Test
    public void authenticationMethodChanges()
    {
        assertEquals(AuthenticationType.JWT.toString().toLowerCase(), getDescriptor().get("authentication").getAsJsonObject().get("type").getAsString());
    }

    @Test
    public void publicKeyDisappears()
    {
        assertNull(getDescriptor().get("authentication").getAsJsonObject().get("publicKey"));
    }

    @Test
    public void installedCallbackRemainsTheSame()
    {
        assertEquals(oAuthAddOnBean.getLifecycle().getInstalled(), getDescriptor().get("lifecycle").getAsJsonObject().get("installed").getAsString());
    }

    @Test
    public void sharedSecretIsNotOldPublicKey()
    {
        assertFalse(oAuthAddOnBean.getAuthentication().getPublicKey().equals(connectAddonRegistry.getSecret(jwtPlugin.getKey())));
    }

    @Test
    public void installedCallbackContainsCorrectKey()
    {
        assertEquals(oAuthAddOnBean.getKey(), getLastInstallPayload().get("key").getAsString());
    }

    @Test
    public void installedCallbackContainsSharedSecret()
    {
        assertNotNull(getLastInstallPayload().get("sharedSecret").getAsString());
    }

    @Test
    public void installedCallbackContainsSameSharedSecretAsRegistry()
    {
        assertEquals(connectAddonRegistry.getSecret(jwtPlugin.getKey()), getLastInstallPayload().get("sharedSecret").getAsString());
    }

    @Test
    public void installedApplinkIsCorrectWhenExistingApplinkPresent() throws IOException {
        // need to clean up first
        uninstall(jwtPlugin);

        /*
         * ACDEV-1417: For this case we need an existing connect like applink but no corresponding addon
         */

        // fake an existing applink. Note hard to fake oauth as requires addon to exist for permission check
        connectApplinkManager.createAppLink(oAuthAddOnBean, oAuthAddOnBean.getBaseUrl(), AuthenticationType.NONE,
                oAuthAddOnBean.getAuthentication().getPublicKey(), "the old key");

        ApplicationLink existingApplink = connectApplinkManager.getAppLink(jwtPlugin.getKey());

        // check the created applink is as expected
        assertEquals("NONE", existingApplink.getProperty(AuthenticationMethod.PROPERTY_NAME));
        assertEquals("the old key", existingApplink.getProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME));
        Object origSharedSecret = existingApplink.getProperty(JwtConstants.AppLinks.SHARED_SECRET_PROPERTY_NAME);
        assertEquals(null, origSharedSecret);


        jwtPlugin = testPluginInstaller.installAddon(createJwtAddOn(oAuthAddOnBean));

        // make sure the applink now reflects what we expect
        ApplicationLink jwtApplink = connectApplinkManager.getAppLink(jwtPlugin.getKey());
        assertEquals("JWT", jwtApplink.getProperty(AuthenticationMethod.PROPERTY_NAME));
        Object newSharedSecret = jwtApplink.getProperty(JwtConstants.AppLinks.SHARED_SECRET_PROPERTY_NAME);
        assertFalse(ObjectUtils.equals(origSharedSecret, newSharedSecret));

        assertEquals(ConnectAddOnUserUtil.usernameForAddon(jwtPlugin.getKey()), jwtApplink.getProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME));
        assertEquals(jwtPlugin.getKey(), jwtApplink.getProperty(JwtConstants.AppLinks.ADD_ON_ID_PROPERTY_NAME));
        assertEquals(Boolean.FALSE.toString(), jwtApplink.getProperty("IS_ACTIVITY_ITEM_PROVIDER"));
        assertEquals(Boolean.TRUE.toString(), jwtApplink.getProperty("system"));
        assertEquals(newSharedSecret, getLastInstallPayload().get("sharedSecret").getAsString());

    }

    @Test
    public void installedApplinkIsCorrectWhenExistingJwtApplinkPresent() throws IOException {
        // need to clean up first
        uninstall(jwtPlugin);

        /*
         * ACDEV-1417: For this case we need an existing connect like applink but no corresponding addon
         */

        // fake an existing applink. Note hard to fake oauth as requires addon to exist for permission check
        connectApplinkManager.createAppLink(oAuthAddOnBean, oAuthAddOnBean.getBaseUrl(), AuthenticationType.JWT,
                "seeeeecret", "the old key");

        ApplicationLink existingApplink = connectApplinkManager.getAppLink(jwtPlugin.getKey());

        // check the created applink is as expected
        assertEquals("JWT", existingApplink.getProperty(AuthenticationMethod.PROPERTY_NAME));
        assertEquals("the old key", existingApplink.getProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME));
        Object origSharedSecret = existingApplink.getProperty(JwtConstants.AppLinks.SHARED_SECRET_PROPERTY_NAME);
        assertEquals("seeeeecret", origSharedSecret);


        jwtPlugin = testPluginInstaller.installAddon(createJwtAddOn(oAuthAddOnBean));

        // make sure the applink now reflects what we expect
        ApplicationLink jwtApplink = connectApplinkManager.getAppLink(jwtPlugin.getKey());
        assertEquals("JWT", jwtApplink.getProperty(AuthenticationMethod.PROPERTY_NAME));
        Object newSharedSecret = jwtApplink.getProperty(JwtConstants.AppLinks.SHARED_SECRET_PROPERTY_NAME);
        assertFalse(ObjectUtils.equals(origSharedSecret, newSharedSecret));

        assertEquals(ConnectAddOnUserUtil.usernameForAddon(jwtPlugin.getKey()), jwtApplink.getProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME));
        assertEquals(jwtPlugin.getKey(), jwtApplink.getProperty(JwtConstants.AppLinks.ADD_ON_ID_PROPERTY_NAME));
        assertEquals(Boolean.FALSE.toString(), jwtApplink.getProperty("IS_ACTIVITY_ITEM_PROVIDER"));
        assertEquals(Boolean.TRUE.toString(), jwtApplink.getProperty("system"));
        assertEquals(newSharedSecret, getLastInstallPayload().get("sharedSecret").getAsString());

    }

    private JsonObject getLastInstallPayload()
    {
        ServletRequestSnapshot installRequest = testFilterResults.getRequest(jwtPlugin.getKey(), oAuthAddOnBean.getLifecycle().getInstalled());
        return new JsonParser().parse(installRequest.getEntity()).getAsJsonObject();
    }

    private JsonObject getDescriptor()
    {
        return new JsonParser().parse(connectAddonRegistry.getDescriptor(jwtPlugin.getKey())).getAsJsonObject();
    }

    private ConnectAddonBean createJwtAddOn(ConnectAddonBean oAuthAddOn)
    {
        AuthenticationBean authenticationBean = AuthenticationBean.newAuthenticationBean()
                .withType(AuthenticationType.JWT)
                .build();
        return createAddonBean(authenticationBean, oAuthAddOn.getKey());
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
        return createAddonBean(authenticationBean, key);
    }

    private ConnectAddonBean createAddonBean(AuthenticationBean authenticationBean, String key)
    {
        return new ConnectAddonBeanBuilder()
                .withKey(key)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withAuthentication(authenticationBean)
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withModule("webItems", randomWebItemBean())
                .build();
    }
}
