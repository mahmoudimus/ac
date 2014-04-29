package it.com.atlassian.plugin.connect.installer;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnaphot;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.user.UserManager;

import com.google.gson.JsonParser;

import org.bouncycastle.openssl.PEMWriter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.TestAuthenticator;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AtlassianPluginsTestRunner.class)
public class AddonLifecycleOAuthTest extends AbstractAddonLifecycleTest
{
    protected AddonLifecycleOAuthTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, AddonTestFilterResults testFilterResults, ConnectApplinkManager connectApplinkManager, ConnectAddOnUserService connectAddOnUserService,UserManager userManager,ApplicationService applicationService,ApplicationManager applicationManager)
    {
        super(testPluginInstaller, testAuthenticator, testFilterResults, connectApplinkManager, connectAddOnUserService, userManager, applicationService, applicationManager);
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
        
        initBeans(newAuthenticationBean()
                .withType(AuthenticationType.OAUTH)
                .withPublicKey(publicKeyWriter.toString())
                .build());
    }

    @Test
    public void installPostContainsNoSharedSecret() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();
            
            ServletRequestSnaphot request = testFilterResults.getRequest(addonKey, INSTALLED);
            String payload = request.getEntity();
            
            boolean hasSharedSecret = new JsonParser().parse(payload).getAsJsonObject().has(SHARED_SECRET_FIELD_NAME);
            assertTrue("field " + SHARED_SECRET_FIELD_NAME + " found in request payload: " + payload, !hasSharedSecret);

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            if(null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }

    @Test
    public void installPostContainsUserKey() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            ServletRequestSnaphot request = testFilterResults.getRequest(addonKey, INSTALLED);
            String payload = request.getEntity();
            assertTrue("field " + USER_KEY_FIELD_NAME + " not found in request payload: " + payload,new JsonParser().parse(payload).getAsJsonObject().has(USER_KEY_FIELD_NAME));

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            if(null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }

    @Test
    public void installPostContainsOAuthLink() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            ServletRequestSnaphot request = testFilterResults.getRequest(addonKey, INSTALLED);
            String payload = request.getEntity();

            boolean hasOauthLink = new JsonParser().parse(payload).getAsJsonObject()
                            .get("links").getAsJsonObject()
                            .get("oauth").getAsString().endsWith("/rest/atlassian-connect/latest/oauth");
                
            assertTrue("OAuth link not found in request payload: " + payload, hasOauthLink);

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            if(null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
        
        
    }

    @Test
    public void uninstallPostContainsNoSharedSecret() throws Exception
    {
        ConnectAddonBean addon = uninstallOnlyBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            testPluginInstaller.uninstallAddon(plugin);
            plugin = null;

            ServletRequestSnaphot request = testFilterResults.getRequest(addonKey, UNINSTALLED);
            String payload = request.getEntity();

            boolean hasSharedSecret = new JsonParser().parse(payload).getAsJsonObject().has(SHARED_SECRET_FIELD_NAME);
            assertTrue("field " + SHARED_SECRET_FIELD_NAME + " found in request payload: " + payload, !hasSharedSecret);

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, UNINSTALLED);
            if(null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }

    @Test
    public void uninstallPostContainsUserKey() throws Exception
    {
        ConnectAddonBean addon = uninstallOnlyBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            testPluginInstaller.uninstallAddon(plugin);
            plugin = null;

            ServletRequestSnaphot request = testFilterResults.getRequest(addonKey, UNINSTALLED);
            String payload = request.getEntity();
            assertTrue("field " + USER_KEY_FIELD_NAME + " not found in request payload: " + payload,new JsonParser().parse(payload).getAsJsonObject().has(USER_KEY_FIELD_NAME));

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, UNINSTALLED);
            if(null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }

    @Test
    public void uninstallPostContainsOAuthLink() throws Exception
    {
        ConnectAddonBean addon = uninstallOnlyBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            testPluginInstaller.uninstallAddon(plugin);
            plugin = null;

            ServletRequestSnaphot request = testFilterResults.getRequest(addonKey, UNINSTALLED);
            String payload = request.getEntity();

            boolean hasOauthLink = new JsonParser().parse(payload).getAsJsonObject()
                                                   .get("links").getAsJsonObject()
                                                   .get("oauth").getAsString().endsWith("/rest/atlassian-connect/latest/oauth");

            assertTrue("OAuth link not found in request payload: " + payload,hasOauthLink);

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, UNINSTALLED);
            if(null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }


    }

}
