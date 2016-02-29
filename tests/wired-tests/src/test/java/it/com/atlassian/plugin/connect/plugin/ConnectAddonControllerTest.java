package it.com.atlassian.plugin.connect.plugin;

import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.api.ConnectAddonController;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonEnableException;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonInstallException;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.AddonUtil;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.util.io.TestFileReader;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.atlassian.plugin.connect.testsupport.util.AddonUtil.randomWebItemBean;
import static org.junit.Assert.assertTrue;

@RunWith(AtlassianPluginsTestRunner.class)
public class ConnectAddonControllerTest {
    private final ConnectAddonController connectAddonController;
    private final TestPluginInstaller testPluginInstaller;
    private final ConnectAddonRegistry addonRegistry;


    public ConnectAddonControllerTest(final ConnectAddonController connectAddonController,
                                      final TestPluginInstaller testPluginInstaller,
                                      final ConnectAddonRegistry addonRegistry) {
        this.connectAddonController = connectAddonController;
        this.testPluginInstaller = testPluginInstaller;
        this.addonRegistry = addonRegistry;
    }

    @Test
    public void canInstallValidAddon() throws ConnectAddonInstallException {
        final String addonKey = "ac-test-" + AddonUtil.randomPluginKey();
        connectAddonController.installAddon(generateValidDescriptor(addonKey));
        assertTrue("ConnectAddonController should successfully install a valid add-on", addonRegistry.hasAddonWithKey(addonKey));
    }

    @Test(expected = ConnectAddonInstallException.class)
    public void InstallingInvalidAddonThrowsException() throws IOException, ConnectAddonInstallException {
        connectAddonController.installAddon(TestFileReader.readAddonTestFile("invalidGenericDescriptor.json"));
    }

    @Test
    public void canUninstallAddon() throws IOException, ConnectAddonInstallException {
        final String addonKey = "ac-test-" + AddonUtil.randomPluginKey();
        testPluginInstaller.installAddon(generateValidDescriptor(addonKey));
        connectAddonController.uninstallAddon(addonKey);
        assertTrue(addonRegistry.getRestartState(addonKey).equals(PluginState.UNINSTALLED));
    }

    @Test
    public void canEnableInstalledAddon() throws IOException, ConnectAddonEnableException {
        final String addonKey = "ac-test-" + AddonUtil.randomPluginKey();
        testPluginInstaller.installAddon(generateValidDescriptor(addonKey));
        testPluginInstaller.disableAddon(addonKey);
        assertTrue(addonRegistry.getRestartState(addonKey).equals(PluginState.DISABLED));
        connectAddonController.enableAddon(addonKey);
        assertTrue(addonRegistry.getRestartState(addonKey).equals(PluginState.ENABLED));
    }

    @Test(expected = ConnectAddonEnableException.class)
    public void EnablingAddonNotInstalledThrowsException() throws IOException, ConnectAddonEnableException {
        connectAddonController.enableAddon("bad-key");
    }

    @Test
    public void canDisableAddon() throws IOException {
        final String addonKey = "ac-test-" + AddonUtil.randomPluginKey();
        testPluginInstaller.installAddon(generateValidDescriptor(addonKey));
        assertTrue(addonRegistry.getRestartState(addonKey).equals(PluginState.ENABLED));
        connectAddonController.disableAddon(addonKey);
        assertTrue(addonRegistry.getRestartState(addonKey).equals(PluginState.DISABLED));
    }

    private String generateValidDescriptor(final String addonKey) {
        final ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
                .withKey(addonKey)
                .withLicensing(false)
                .withDescription(ConnectAddonAccessorTest.class.getCanonicalName())
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withModule("webItems", randomWebItemBean())
                .build();

        return ConnectModulesGsonFactory.addonBeanToJson(addonBean);
    }
}
