package it.com.atlassian.plugin.connect.installer;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.openssl.PEMWriter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.filter.AddonTestFilterResults;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.TestPluginInstaller;
import it.com.atlassian.plugin.connect.filter.ServletRequestSnaphot;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AtlassianPluginsTestRunner.class)
public class AddonLifecycleOAuthTest
{
    public static final String PLUGIN_KEY = "my-oauth-plugin";
    public static final String PLUGIN_NAME = "My Plugin";
    public static final String INSTALLED = "/installed";
    public static final String UNINSTALLED = "/uninstalled";
    public static final String SHARED_SECRET_FIELD_NAME = "sharedSecret";
    public static final String USER_KEY_FIELD_NAME = "userKey";
    
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final AddonTestFilterResults testFilterResults;
    private final ConnectApplinkManager connectApplinkManager;
    
    private ConnectAddonBean baseBean;
    private ConnectAddonBean installOnlyBean;
    private ConnectAddonBean uninstallOnlyBean;
    

    public AddonLifecycleOAuthTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, AddonTestFilterResults addonTestFilterResults, ConnectApplinkManager connectApplinkManager)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.testFilterResults = addonTestFilterResults;
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
                    .withType(AuthenticationType.OAUTH)
                    .withPublicKey(publicKeyWriter.toString())
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

        this.uninstallOnlyBean = newConnectAddonBean(baseBean)
                .withLifecycle(
                        newLifecycleBean()
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
            if(null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }
    
    @Test
    public void installPostContainsNoSharedSecret() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            ServletRequestSnaphot request = testFilterResults.getRequest(PLUGIN_KEY, INSTALLED);
            String payload = request.getEntity();
            
            boolean hasSharedSecret = new JsonParser().parse(payload).getAsJsonObject().has(SHARED_SECRET_FIELD_NAME);
            assertTrue("field " + SHARED_SECRET_FIELD_NAME + " found in request payload: " + payload, !hasSharedSecret);

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, INSTALLED);
            if(null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void installPostContainsUserKey() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            ServletRequestSnaphot request = testFilterResults.getRequest(PLUGIN_KEY, INSTALLED);
            String payload = request.getEntity();
            assertTrue("field " + USER_KEY_FIELD_NAME + " not found in request payload: " + payload,new JsonParser().parse(payload).getAsJsonObject().has(USER_KEY_FIELD_NAME));

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, INSTALLED);
            if(null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void installPostContainsOAuthLink() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            ServletRequestSnaphot request = testFilterResults.getRequest(PLUGIN_KEY, INSTALLED);
            String payload = request.getEntity();

            boolean hasOauthLink = new JsonParser().parse(payload).getAsJsonObject()
                            .get("links").getAsJsonObject()
                            .get("oauth").getAsString().endsWith("/rest/atlassian-connect/latest/oauth");
                
            assertTrue("OAuth link not found in request payload: " + payload, hasOauthLink);

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, INSTALLED);
            if(null != plugin)
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
            if(null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void uninstallPostContainsNoSharedSecret() throws Exception
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

            boolean hasSharedSecret = new JsonParser().parse(payload).getAsJsonObject().has(SHARED_SECRET_FIELD_NAME);
            assertTrue("field " + SHARED_SECRET_FIELD_NAME + " found in request payload: " + payload, !hasSharedSecret);

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, UNINSTALLED);
            if(null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void uninstallPostContainsUserKey() throws Exception
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
            assertTrue("field " + USER_KEY_FIELD_NAME + " not found in request payload: " + payload,new JsonParser().parse(payload).getAsJsonObject().has(USER_KEY_FIELD_NAME));

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, UNINSTALLED);
            if(null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

    @Test
    public void uninstallPostContainsOAuthLink() throws Exception
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

            boolean hasOauthLink = new JsonParser().parse(payload).getAsJsonObject()
                                                   .get("links").getAsJsonObject()
                                                   .get("oauth").getAsString().endsWith("/rest/atlassian-connect/latest/oauth");

            assertTrue("OAuth link not found in request payload: " + payload,hasOauthLink);

        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, UNINSTALLED);
            if(null != plugin)
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
            assertEquals(addon.getBaseUrl(),appLink.getDisplayUrl().toString());
            assertEquals("addon_" + addon.getKey(),appLink.getProperty("user.key"));

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
