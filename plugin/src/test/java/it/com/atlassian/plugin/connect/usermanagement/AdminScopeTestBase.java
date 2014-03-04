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
import com.atlassian.user.EntityException;
import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

public abstract class AdminScopeTestBase
{
    private final TestPluginInstaller testPluginInstaller;
    private final JwtApplinkFinder jwtApplinkFinder;

    private Plugin plugin;

    public AdminScopeTestBase(TestPluginInstaller testPluginInstaller,
                              JwtApplinkFinder jwtApplinkFinder)
    {
        this.testPluginInstaller = checkNotNull(testPluginInstaller);
        this.jwtApplinkFinder = checkNotNull(jwtApplinkFinder);
    }

    @Test
    public void hasCorrectAdminStatus() throws EntityException
    {
        ApplicationLink appLink = jwtApplinkFinder.find(plugin.getKey());
        String username = (String) appLink.getProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME);
        assertEquals(shouldBeAdmin(), isAdmin(username));
    }

    protected abstract ScopeName getScope();
    protected abstract boolean shouldBeAdmin();
    protected abstract boolean isAdmin(String username) throws EntityException;

    @BeforeClass
    public void setUp() throws IOException
    {
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
