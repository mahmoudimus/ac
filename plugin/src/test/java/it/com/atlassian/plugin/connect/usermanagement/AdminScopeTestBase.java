package it.com.atlassian.plugin.connect.usermanagement;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.google.common.collect.ImmutableSet;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

public abstract class AdminScopeTestBase
{
    private final static Logger LOG = LoggerFactory.getLogger(AdminScopeTestBase.class);

    protected final TestPluginInstaller testPluginInstaller;
    private final JwtApplinkFinder jwtApplinkFinder;
    private final TestAuthenticator testAuthenticator;

    private Plugin plugin;
    private ConnectAddonBean addonBaseBean;

    public AdminScopeTestBase(TestPluginInstaller testPluginInstaller,
                              JwtApplinkFinder jwtApplinkFinder,
                              TestAuthenticator testAuthenticator)
    {
        this.testPluginInstaller = checkNotNull(testPluginInstaller);
        this.jwtApplinkFinder = checkNotNull(jwtApplinkFinder);
        this.testAuthenticator = checkNotNull(testAuthenticator);
    }

    @Test
    public void hasCorrectTopLevelAdminStatus()
    {
        assertEquals(shouldBeTopLevelAdmin(), isUserTopLevelAdmin(getAddonUsername()));
    }

    @Test
    public void isNotTopLevelAdminAfterDowngrade() throws Exception
    {
        installLowerScopeAddon();
        assertEquals(false, isUserTopLevelAdmin(getAddonUsername()));
    }

    @Test
    public void hasCorrectTopLevelAdminStatusAfterUpgrade() throws IOException
    {
        installLowerScopeAddon();
        installPlugin();
        assertEquals(shouldBeTopLevelAdmin(), isUserTopLevelAdmin(getAddonUsername()));
    }

    protected abstract ScopeName getScope();
    protected abstract ScopeName getScopeOneDown();
    protected abstract boolean shouldBeTopLevelAdmin();
    protected abstract boolean isUserTopLevelAdmin(String username);

    protected void installLowerScopeAddon() throws IOException
    {
        ConnectAddonBean lowerScopeBean = deriveNewAddon()
                .withScopes(ImmutableSet.of(getScopeOneDown())) // because "one lower than admin" is product specific
                .build();
        installAddon(lowerScopeBean);
    }

    protected ConnectAddonBeanBuilder deriveNewAddon()
    {
        return ConnectAddonBean.newConnectAddonBean(addonBaseBean);
    }

    protected void installAddon(ConnectAddonBean addon) throws IOException
    {
        plugin = testPluginInstaller.installPlugin(addon);
    }

    protected String getAddonUsername()
    {
        return getAddonUsername(plugin);
    }

    protected String getAddonUsername(Plugin aPlugin)
    {
        checkArgument(null != plugin, "'plugin' must not be null!");
        ApplicationLink appLink = jwtApplinkFinder.find(aPlugin.getKey());
        return (String) appLink.getProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME);
    }

    @Before
    public void setUp() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
        plugin = installPlugin();
        checkArgument(null != plugin, "'plugin' data member should not be null after installation: check the logs for installation messages");
    }

    @After
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
        addonBaseBean = ConnectAddonBean.newConnectAddonBean()
                .withKey(key)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .build();

        ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean(addonBaseBean)
                .withScopes(ImmutableSet.of(getScope()))
                .build();

        LOG.info("Installing test plugin '{}'", addonBean.getKey());
        return testPluginInstaller.installPlugin(addonBean);
    }
}
