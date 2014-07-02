package it.com.atlassian.plugin.connect.installer;

import java.io.IOException;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonPrecannedResponseHelper;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilter;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.upm.spi.PluginInstallException;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.test.util.AddonUtil.randomWebItemBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

@RunWith(AtlassianPluginsTestRunner.class)
public class AddOnInstallTest
{
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final JwtApplinkFinder jwtApplinkFinder;
    private final AddonPrecannedResponseHelper addonTestFilter;

    private Plugin plugin;

    public AddOnInstallTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
                            JwtApplinkFinder jwtApplinkFinder, AddonPrecannedResponseHelper addonTestFilter)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.jwtApplinkFinder = jwtApplinkFinder;
        this.addonTestFilter = addonTestFilter;
    }

    @Before
    public void setUp() throws IOException
    {
        testAuthenticator.authenticateUser("admin");

//        addonTestFilter.queuePrecannedResponse("installed", 500);
//        plugin = installPlugin();
//        testPluginInstaller.disableAddon(plugin.getKey());
    }

    @After
    public void tearDown() throws IOException
    {
        if (null != plugin)
        {
            testPluginInstaller.uninstallAddon(plugin);
        }
    }

    @Test
    public void blah() throws IOException
    {
//        plugin = installPlugin();
//        final String originalSharedSecret = getSharedSecret();

        addonTestFilter.queuePrecannedResponse("installed", 404);
//        addonTestFilter.queuePrecannedResponse("uninstalled", 404);
        try
        {
            plugin = installPlugin();
            fail("should have received PluginInstallException");
        }
        catch (PluginInstallException e)
        {

        }

//        final String secondSharedSecret = getSharedSecret();

//        plugin = installPlugin();
//
//        final String finalSharedSecret = getSharedSecret();
//
////        assertNotEquals(originalSharedSecret, newSharedSecret);
//        assertFalse(StringUtils.equals(originalSharedSecret, finalSharedSecret));
    }

    private String getSharedSecret()
    {
        if (plugin == null)
        {
            return null;
        }
        ApplicationLink appLink = jwtApplinkFinder.find(plugin.getKey());
        if (appLink == null)
        {
            return null;
        }
        final Object ss = appLink.getProperty(JwtConstants.AppLinks.SHARED_SECRET_PROPERTY_NAME);
        return ss == null ? null : ss.toString();
    }


    private Plugin installPlugin() throws IOException
    {
        String key = "ac-test-" + System.currentTimeMillis();
        ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
            .withKey(key)
            .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
            .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
            .withLifecycle(LifecycleBean.newLifecycleBean()
                    .withInstalled("/installed")
                    .withUninstalled("/uninstalled")
                    .build())
            .withModule("webItems",randomWebItemBean())
            .build();

        return testPluginInstaller.installAddon(addonBean);
    }
}
