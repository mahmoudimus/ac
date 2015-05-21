package it.com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.connect.api.service.AddOnService;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.atlassian.plugin.connect.testsupport.util.AddonUtil.randomWebItemBean;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith (AtlassianPluginsTestRunner.class)
public class AddOnServiceTest
{
    private final TestPluginInstaller testPluginInstaller;
    private final AddOnService addOnService;

    public AddOnServiceTest(final TestPluginInstaller testPluginInstaller, final AddOnService addOnService)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.addOnService = addOnService;
    }

    @Test
    public void testIsAddOnEnabled() throws IOException
    {
        final String addonKey = "ac-test " + System.currentTimeMillis();
        installPlugin(addonKey);

        assertTrue("AddOnService is expected to return true for enabled add-ons", addOnService.isAddOnEnabled(addonKey));

        testPluginInstaller.uninstallAddon(addonKey);
    }

    @Test
    public void testAddOnIsNotEnabled()
    {
        assertFalse("AddOnService is expected to return false for disabled add-ons", addOnService.isAddOnEnabled("some-random-key" + System.currentTimeMillis()));
    }

    private void installPlugin(final String addonKey) throws IOException
    {
        final ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
                .withKey(addonKey)
                .withDescription(AddOnServiceTest.class.getCanonicalName())
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withModule("webItems", randomWebItemBean())
                .build();

        testPluginInstaller.installAddon(addonBean);
    }

}
