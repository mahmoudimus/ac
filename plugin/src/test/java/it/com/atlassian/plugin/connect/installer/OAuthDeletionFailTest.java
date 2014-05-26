package it.com.atlassian.plugin.connect.installer;

import java.io.IOException;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

//TODO: Just delete this class
@RunWith(AtlassianPluginsTestRunner.class)
public class OAuthDeletionFailTest
{

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ConnectApplinkManager applinkManager;
    private Plugin oAuthPlugin;
    private ConnectAddonBean oAuthAddOnBean;

    public OAuthDeletionFailTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
                                 ConnectApplinkManager applinkManager)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.applinkManager = applinkManager;
    }

    @BeforeClass
    public void beforeAllTests() throws IOException
    {
        oAuthAddOnBean = createOAuthAddOnBean();

        //you MUST login as admin before you can use the testPluginInstaller
        testAuthenticator.authenticateUser("admin");

        oAuthPlugin = testPluginInstaller.installAddon(oAuthAddOnBean);
    }

    @Test
    public void testUninstallWhenUnauthenticated() throws Exception
    {
        assertNotNull(applinkManager.getAppLink(oAuthAddOnBean.getKey()));
        testAuthenticator.unauthenticate();
        testPluginInstaller.uninstallPlugin(oAuthPlugin);
        assertNull(applinkManager.getAppLink(oAuthAddOnBean.getKey()));
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
                .withModule("generalPages", ConnectPageModuleBean.newPageBean()
                                .withKey("foo")
                                .withName(new I18nProperty("daName", "The Name"))
                                .withUrl("/foo")
                                .build()
                )
                .build();
    }
}
