package it.com.atlassian.plugin.connect.usermanagement;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.google.common.collect.ImmutableSet;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

public abstract class AdminScopeTestBase
{
    protected final TestPluginInstaller testPluginInstaller;
    private final JwtApplinkFinder jwtApplinkFinder;
    private final TestAuthenticator testAuthenticator;

    private Plugin plugin;

    public AdminScopeTestBase(TestPluginInstaller testPluginInstaller,
                              JwtApplinkFinder jwtApplinkFinder,
                              TestAuthenticator testAuthenticator)
    {
        this.testPluginInstaller = checkNotNull(testPluginInstaller);
        this.jwtApplinkFinder = checkNotNull(jwtApplinkFinder);
        this.testAuthenticator = checkNotNull(testAuthenticator);
    }

    @Test
    public void hasCorrectAdminStatus()
    {
        assertEquals(shouldBeAdmin(), isUserAdmin(getAddonUsername()));
    }

    protected abstract ScopeName getScope();
    protected abstract boolean shouldBeAdmin();
    protected abstract boolean isUserAdmin(String username);

    protected String getAddonUsername()
    {
        ApplicationLink appLink = jwtApplinkFinder.find(plugin.getKey());
        return (String) appLink.getProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME);
    }

    @BeforeClass
    public void setUp() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
        plugin = installPlugin();
    }

    @AfterClass
    public void tearDown() throws IOException
    {
        if (null != plugin)
        {
            testPluginInstaller.uninstallPlugin(plugin);
        }
    }

    private Plugin installPlugin() throws IOException
    {
        String key = "ac-test-" + System.currentTimeMillis();
        ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
                .withKey(key)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withScopes(ImmutableSet.of(getScope()))
                .build();

        return testPluginInstaller.installPlugin(addonBean);
    }
}
