package it.com.atlassian.plugin.connect.plugin.usermanagement;

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
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.atlassian.plugin.connect.testsupport.util.AddonUtil.randomWebItemBean;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

public abstract class AdminScopeTestBase
{
    private final static Logger LOG = LoggerFactory.getLogger(AdminScopeTestBase.class);

    private final TestPluginInstaller testPluginInstaller;
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
    public void hasCorrectTopLevelAdminStatus() throws IOException
    {
        plugin = installPlugin(getScope());
        assertEquals(shouldBeTopLevelAdmin(), isUserTopLevelAdmin(getAddonUsername(plugin)));
    }

    @Test
    public void isNotTopLevelAdminAfterDowngrade() throws Exception
    {
        plugin = installPlugin(getScope());
        plugin = installPlugin(getScopeOneDown());
        assertEquals(false, isUserTopLevelAdmin(getAddonUsername(plugin)));
    }

    @Test
    public void hasCorrectTopLevelAdminStatusAfterUpgrade() throws IOException
    {
        plugin = installPlugin(getScopeOneDown());
        plugin = installPlugin(getScope());
        assertEquals(shouldBeTopLevelAdmin(), isUserTopLevelAdmin(getAddonUsername(plugin)));
    }

    /**
     * The UPM executes upgrades on a task scheduler thread that has no principal in the authentication context.
     */
    @Test
    public void canUpgradeAnonymously() throws Exception
    {
        testAuthenticator.unauthenticate();
        plugin = installPlugin(getScopeOneDown());
        plugin = installPlugin(getScope());
        assertEquals(shouldBeTopLevelAdmin(), isUserTopLevelAdmin(getAddonUsername(plugin)));
    }

    @Test
    public void isNoLongerTopLevelAdminAfterReinstallWithDowngradedScope() throws IOException
    {
        plugin = installPlugin(getScope());
        testPluginInstaller.uninstallAddon(plugin);
        plugin = installPlugin(getScopeOneDown());
        assertEquals(false, isUserTopLevelAdmin(getAddonUsername(plugin)));
    }

    protected abstract ScopeName getScope();
    protected abstract ScopeName getScopeOneDown();
    protected abstract boolean shouldBeTopLevelAdmin();
    protected abstract boolean isUserTopLevelAdmin(String username);

    protected String getAddonUsername(Plugin plugin) throws IOException
    {
        ApplicationLink appLink = jwtApplinkFinder.find(plugin.getKey());
        return (String) appLink.getProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME);
    }

    @Before
    public void setUp() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
        String key = "ac-TEST-" + System.currentTimeMillis(); // Use uppercase character to detect username vs userkey issues
        addonBaseBean = ConnectAddonBean.newConnectAddonBean()
                .withKey(key)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withDescription(getClass().getCanonicalName())
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withModule("webItems", randomWebItemBean())
                .build();
    }

    @After
    public void tearDown() throws IOException
    {
        if (null != plugin)
        {
            testPluginInstaller.uninstallAddon(plugin);
        }
        testAuthenticator.unauthenticate();
    }

    private Plugin installPlugin(ScopeName scope) throws IOException
    {
        ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean(addonBaseBean)
                .withScopes(ImmutableSet.of(scope))
                .build();

        LOG.warn("Installing test plugin '{}'", addonBean.getKey());
        Plugin installedPlugin = testPluginInstaller.installAddon(addonBean);
        checkArgument(null != installedPlugin, "'installedPlugin' should not be null after installation: check the logs for installation messages");
        return installedPlugin;
    }
}
