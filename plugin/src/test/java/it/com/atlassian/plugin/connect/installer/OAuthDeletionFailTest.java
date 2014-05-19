package it.com.atlassian.plugin.connect.installer;

import java.io.IOException;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonRegistry;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.com.atlassian.plugin.connect.TestAuthenticator;

//TODO: Just delete this class
@RunWith(AtlassianPluginsTestRunner.class)
public class OAuthDeletionFailTest
{
    private static final Logger LOG = LoggerFactory.getLogger(OAuthDeletionFailTest.class);

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final AddonTestFilterResults testFilterResults;
    private Plugin oAuthPlugin;
    private ConnectAddonBean oAuthAddOnBean;

    public OAuthDeletionFailTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, ConnectAddonRegistry connectAddonRegistry, AddonTestFilterResults testFilterResults)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.connectAddonRegistry = connectAddonRegistry;
        this.testFilterResults = testFilterResults;
    }

    @BeforeClass
    public void beforeAllTests() throws IOException
    {
        oAuthAddOnBean = createOAuthAddOnBean();

        //you MUST login as admin before you can use the testPluginInstaler
        testAuthenticator.authenticateUser("admin");

        oAuthPlugin = testPluginInstaller.installPlugin(oAuthAddOnBean);
    }

    @Test
    public void badData() throws Exception
    {
        System.out.println("i installed something");
        
        testAuthenticator.unauthenticate();
        testPluginInstaller.uninstallPlugin(oAuthPlugin);

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
                .build();
    }
}
