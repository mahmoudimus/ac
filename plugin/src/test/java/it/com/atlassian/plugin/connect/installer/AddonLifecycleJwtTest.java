package it.com.atlassian.plugin.connect.installer;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.util.WaitUntil;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import com.google.gson.JsonParser;

import org.bouncycastle.openssl.PEMWriter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.TestPluginInstaller;
import it.com.atlassian.plugin.connect.filter.AddonTestFilterResults;
import it.com.atlassian.plugin.connect.filter.JwtTestVerifier;
import it.com.atlassian.plugin.connect.filter.ServletRequestSnaphot;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static org.junit.Assert.*;

@RunWith(AtlassianPluginsTestRunner.class)
public class AddonLifecycleJwtTest
{
    public static final String PLUGIN_KEY = "my-jwt-plugin";
    public static final String PLUGIN_NAME = "My Plugin";
    public static final String INSTALLED = "/installed";
    public static final String ENABLED = "/enabled";
    public static final String UNINSTALLED = "/uninstalled";
    public static final String SHARED_SECRET_FIELD_NAME = "sharedSecret";
    public static final String CLIENT_KEY_FIELD_NAME = "clientKey";
    public static final String USER_KEY_FIELD_NAME = "userKey";

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final AddonTestFilterResults testFilterResults;
    private final ConnectApplinkManager connectApplinkManager;

    private ConnectAddonBean baseBean;
    private ConnectAddonBean installOnlyBean;
    private ConnectAddonBean uninstallOnlyBean;
    private ConnectAddonBean installAndEnabledBean;
    private ConnectAddonBean installAndUninstallBean;

    public AddonLifecycleJwtTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, AddonTestFilterResults testFilterResults, ConnectApplinkManager connectApplinkManager)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.testFilterResults = testFilterResults;
        this.connectApplinkManager = connectApplinkManager;
    }

    @BeforeClass
    public void setup() throws Exception
    {
        testAuthenticator.authenticateUser("admin");

        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        KeyPair oauthKeyPair = gen.generateKeyPair();
        StringWriter publicKeyWriter = new StringWriter();
        PEMWriter pubWriter = new PEMWriter(publicKeyWriter);
        pubWriter.writeObject(oauthKeyPair.getPublic());
        pubWriter.close();

        this.baseBean = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withAuthentication(
                        newAuthenticationBean()
                                .withType(AuthenticationType.JWT)
                                .build()
                )
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(PLUGIN_KEY))
                .build();

        this.installOnlyBean = newConnectAddonBean(baseBean)
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .build()
                )
                .build();

        this.installAndEnabledBean = newConnectAddonBean(baseBean)
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .withEnabled(ENABLED)
                                .build()
                )
                .build();

        this.uninstallOnlyBean = newConnectAddonBean(baseBean)
                .withLifecycle(
                        newLifecycleBean()
                                .withUninstalled(UNINSTALLED)
                                .build()
                )
                .build();

        this.installAndUninstallBean = newConnectAddonBean(baseBean)
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .withUninstalled(UNINSTALLED)
                                .build()
                )
                .build();
    }

    @Test
    public void installUrlIsPosted() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            ServletRequestSnaphot request = testFilterResults.getRequest(PLUGIN_KEY, INSTALLED);
            assertEquals(HttpMethod.POST, request.getMethod());

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, INSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void installPostContainsSharedSecret() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            ServletRequestSnaphot request = testFilterResults.getRequest(PLUGIN_KEY, INSTALLED);
            String payload = request.getEntity();

            boolean hasSharedSecret = new JsonParser().parse(payload).getAsJsonObject().has(SHARED_SECRET_FIELD_NAME);
            assertTrue("field " + SHARED_SECRET_FIELD_NAME + " not found in request payload: " + payload, hasSharedSecret);

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, INSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void enabledPostContainsValidSharedSecret() throws Exception
    {
        ConnectAddonBean addon = installAndEnabledBean;

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            ServletRequestSnaphot installRequest = testFilterResults.getRequest(PLUGIN_KEY, INSTALLED);
            String installPayload = installRequest.getEntity();

            String sharedSecret = new JsonParser().parse(installPayload).getAsJsonObject().get(SHARED_SECRET_FIELD_NAME).getAsString();
            String clientKey = new JsonParser().parse(installPayload).getAsJsonObject().get(CLIENT_KEY_FIELD_NAME).getAsString();

            WaitUntil.invoke(new WaitUntil.WaitCondition()
            {
                @Override
                public boolean isFinished()
                {
                    return null != testFilterResults.getRequest(PLUGIN_KEY, ENABLED);
                }

                @Override
                public String getWaitMessage()
                {
                    return "waiting for enable webhook post...";
                }
            });

            ServletRequestSnaphot enableRequest = testFilterResults.getRequest(PLUGIN_KEY, ENABLED);

            String jwtToken = enableRequest.getHeaders().get(JwtConstants.HttpRequests.AUTHORIZATION_HEADER.toLowerCase());

            JwtTestVerifier verifier = new JwtTestVerifier(sharedSecret, clientKey);

            assertTrue("unverified jwt token", verifier.jwtAndClientAreValid(jwtToken));

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, INSTALLED);
            testFilterResults.clearRequest(PLUGIN_KEY, ENABLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void installPostContainsNoUserKey() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            ServletRequestSnaphot request = testFilterResults.getRequest(PLUGIN_KEY, INSTALLED);
            String payload = request.getEntity();
            boolean hasUserKey = new JsonParser().parse(payload).getAsJsonObject().has(USER_KEY_FIELD_NAME);

            assertTrue("field " + USER_KEY_FIELD_NAME + " found in request payload: " + payload, !hasUserKey);

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, INSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void uninstallUrlIsPosted() throws Exception
    {
        ConnectAddonBean addon = uninstallOnlyBean;

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            testPluginInstaller.uninstallPlugin(plugin);
            plugin = null;

            ServletRequestSnaphot request = testFilterResults.getRequest(PLUGIN_KEY, UNINSTALLED);
            assertEquals(HttpMethod.POST, request.getMethod());

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, UNINSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void uninstallPostContainsValidJwt() throws Exception
    {
        ConnectAddonBean addon = installAndUninstallBean;

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            ServletRequestSnaphot installRequest = testFilterResults.getRequest(PLUGIN_KEY, INSTALLED);
            String installPayload = installRequest.getEntity();

            String sharedSecret = new JsonParser().parse(installPayload).getAsJsonObject().get(SHARED_SECRET_FIELD_NAME).getAsString();
            String clientKey = new JsonParser().parse(installPayload).getAsJsonObject().get(CLIENT_KEY_FIELD_NAME).getAsString();

            testPluginInstaller.uninstallPlugin(plugin);
            plugin = null;

            ServletRequestSnaphot uninstallRequest = testFilterResults.getRequest(PLUGIN_KEY, UNINSTALLED);

            String jwtToken = uninstallRequest.getHeaders().get(JwtConstants.HttpRequests.AUTHORIZATION_HEADER.toLowerCase());

            JwtTestVerifier verifier = new JwtTestVerifier(sharedSecret, clientKey);

            assertTrue("unverified jwt token", verifier.jwtAndClientAreValid(jwtToken));

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, INSTALLED);
            testFilterResults.clearRequest(PLUGIN_KEY, UNINSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void uninstallPostContainsNoUserKey() throws Exception
    {
        ConnectAddonBean addon = uninstallOnlyBean;

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            testPluginInstaller.uninstallPlugin(plugin);
            plugin = null;

            ServletRequestSnaphot request = testFilterResults.getRequest(PLUGIN_KEY, UNINSTALLED);
            String payload = request.getEntity();
            boolean hasUserKey = new JsonParser().parse(payload).getAsJsonObject().has(USER_KEY_FIELD_NAME);

            assertTrue("field " + USER_KEY_FIELD_NAME + " found in request payload: " + payload, !hasUserKey);

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, UNINSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void appLinkIsCreatedWithCorrectParameters() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            ServletRequestSnaphot request = testFilterResults.getRequest(PLUGIN_KEY, INSTALLED);

            ApplicationLink appLink = connectApplinkManager.getAppLink(addon.getKey());

            assertNotNull((appLink));
            assertEquals(addon.getBaseUrl(), appLink.getDisplayUrl().toString());
            assertEquals("addon_" + addon.getKey(), appLink.getProperty("user.key"));

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, INSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

}
