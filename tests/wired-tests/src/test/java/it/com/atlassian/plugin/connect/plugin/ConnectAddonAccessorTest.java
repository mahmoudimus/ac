package it.com.atlassian.plugin.connect.plugin;

import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.AddonUtil;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.atlassian.plugin.connect.testsupport.util.AddonUtil.randomWebItemBean;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AtlassianPluginsTestRunner.class)
public class ConnectAddonAccessorTest {
    private final TestPluginInstaller testPluginInstaller;
    private final ConnectAddonAccessor addonService;

    public ConnectAddonAccessorTest(final TestPluginInstaller testPluginInstaller,
                                    final ConnectAddonAccessor addonService) {
        this.testPluginInstaller = testPluginInstaller;
        this.addonService = addonService;
    }

    @Test
    public void testIsAddonEnabled() throws IOException {
        final String addonKey = "ac-test-" + AddonUtil.randomPluginKey();
        installPlugin(addonKey);

        assertTrue("ConnectAddonAccessor is expected to return true for enabled add-ons", addonService.isAddonEnabled(addonKey));

        testPluginInstaller.uninstallAddon(addonKey);
    }

    @Test
    public void testAddonIsNotEnabled() {
        assertFalse("ConnectAddonAccessor is expected to return false for not installed add-ons", addonService.isAddonEnabled("some-random-key" + AddonUtil.randomPluginKey()));
    }

    @Test
    public void testAddonIsInstalledButNotEnabled() throws IOException {
        final String addonKey = "ac-test-" + AddonUtil.randomPluginKey();
        installPlugin(addonKey);

        testPluginInstaller.disableAddon(addonKey);

        assertFalse("ConnectAddonAccessor is expected to return false for disabled add-ons", addonService.isAddonEnabled(addonKey));

        testPluginInstaller.uninstallAddon(addonKey);
    }

    private void installPlugin(final String addonKey) throws IOException {
        final ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
                .withKey(addonKey)
                .withLicensing(false)
                .withDescription(ConnectAddonAccessorTest.class.getCanonicalName())
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withModule("webItems", randomWebItemBean())
                .build();

        testPluginInstaller.installAddon(addonBean);
    }
}
