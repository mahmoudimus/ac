package it.com.atlassian.plugin.connect.usermanagement.confluence;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.confluence.cache.ThreadLocalCache;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static com.atlassian.confluence.security.SpacePermission.ADMINISTER_SPACE_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.COMMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.CREATEEDIT_PAGE_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.CREATE_ATTACHMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.EDITBLOG_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_ATTACHMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_BLOG_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_COMMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_PAGE_PERMISSION;
import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Application ("confluence")
@RunWith (AtlassianPluginsTestRunner.class)
public class DetailedConfluenceSpaceAdminScopeTest
{
    private static final Logger log = LoggerFactory.getLogger(DetailedConfluenceSpaceAdminScopeTest.class);
    private static final String JEDI_SPACE_KEY = "JEDI" + System.currentTimeMillis();

    private final SpaceManager spaceManager;
    private final SpacePermissionManager spacePermissionManager;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final JwtApplinkFinder jwtApplinkFinder;
    private Space jediSpace;
    private Plugin plugin;

    public DetailedConfluenceSpaceAdminScopeTest(SpaceManager spaceManager,
                                                 SpacePermissionManager spacePermissionManager,
                                                 TestPluginInstaller testPluginInstaller,
                                                 TestAuthenticator testAuthenticator,
                                                 JwtApplinkFinder jwtApplinkFinder)
    {
        this.spaceManager = spaceManager;
        this.spacePermissionManager = spacePermissionManager;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.jwtApplinkFinder = jwtApplinkFinder;
    }

    @Before
    public void setUp() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
        plugin = installPlugin(ScopeName.SPACE_ADMIN);
    }

    @After
    public void cleanup() throws IOException
    {
        if (jediSpace != null)
        {
            try
            {
                spaceManager.removeSpace(jediSpace);
                jediSpace = null;
            }
            catch (Exception e)
            {
                //couldn't delete the space for some reason, just ignore
            }
        }

        if (null != plugin)
        {
            testPluginInstaller.uninstallPlugin(plugin);
        }
    }

    @Test
    public void addonIsMadeAdminOfExistingSpace() throws Exception
    {
        assertIsSpaceAdminOnAllSpaces(spaceManager, spacePermissionManager, getAddonUser());
    }

    @Test
    public void addonIsMadeAdminOfExistingSpaceAfterDowngradeFromTopAdmin() throws Exception
    {
        plugin = installPlugin(ScopeName.ADMIN);
        plugin = installPlugin(ScopeName.SPACE_ADMIN);
        assertIsSpaceAdminOnAllSpaces(spaceManager, spacePermissionManager, getAddonUser());
    }

    private static void assertIsSpaceAdminOnAllSpaces(SpaceManager spaceManager, SpacePermissionManager spacePermissionManager, ConfluenceUser addonUser)
    {
        List<String> permissionErrors = Lists.newArrayList();
        permissionErrors.addAll(checkHasPermissionOnAllSpaces(CREATEEDIT_PAGE_PERMISSION, spaceManager, spacePermissionManager, addonUser));
        permissionErrors.addAll(checkHasPermissionOnAllSpaces(CREATE_ATTACHMENT_PERMISSION, spaceManager, spacePermissionManager, addonUser));
        permissionErrors.addAll(checkHasPermissionOnAllSpaces(COMMENT_PERMISSION, spaceManager, spacePermissionManager, addonUser));
        permissionErrors.addAll(checkHasPermissionOnAllSpaces(EDITBLOG_PERMISSION, spaceManager, spacePermissionManager, addonUser));
        permissionErrors.addAll(checkHasPermissionOnAllSpaces(REMOVE_PAGE_PERMISSION, spaceManager, spacePermissionManager, addonUser));
        permissionErrors.addAll(checkHasPermissionOnAllSpaces(REMOVE_ATTACHMENT_PERMISSION, spaceManager, spacePermissionManager, addonUser));
        permissionErrors.addAll(checkHasPermissionOnAllSpaces(REMOVE_COMMENT_PERMISSION, spaceManager, spacePermissionManager, addonUser));
        permissionErrors.addAll(checkHasPermissionOnAllSpaces(REMOVE_BLOG_PERMISSION, spaceManager, spacePermissionManager, addonUser));
        permissionErrors.addAll(checkHasPermissionOnAllSpaces(ADMINISTER_SPACE_PERMISSION, spaceManager, spacePermissionManager, addonUser));
        assertTrue(StringUtils.join(permissionErrors, '\n'), permissionErrors.isEmpty());
    }

    private static List<String> checkHasPermissionOnAllSpaces(String permission, SpaceManager spaceManager,
                                                              SpacePermissionManager spacePermissionManager, ConfluenceUser addonUser)
    {
        List<Space> allSpaces = spaceManager.getAllSpaces();

        List<String> permissionErrors = Lists.newArrayList();

        for (Space space : allSpaces)
        {
            /*
             * Confluence caches some security stuff on thread local and due to a bug we need to blast it away before checking permission
             */
            ThreadLocalCache.flush();

            boolean canAdminister = spacePermissionManager.hasPermission(permission, space, addonUser);
            if (!canAdminister)
            {
                permissionErrors.add("Add-on user " + addonUser.getName() + " should have " + permission +
                        " permission for space " + space.getKey());
            }
        }

        return permissionErrors;
    }

    @Test
    public void addonIsMadeAdminOfNewSpace() throws Exception
    {
        assertIsSpaceAdminOfNewSpace();
    }

    @Test
    public void addonIsMadeAdminOfNewSpaceAfterDowngradeFromTopAdmin() throws Exception
    {
        plugin = installPlugin(ScopeName.ADMIN);
        plugin = installPlugin(ScopeName.SPACE_ADMIN);
        assertIsSpaceAdminOfNewSpace();
    }

    private void assertIsSpaceAdminOfNewSpace()
    {
        ConfluenceUser admin = FindUserHelper.getUserByUsername("admin");

        jediSpace = spaceManager.createSpace(JEDI_SPACE_KEY, "Knights of the Old Republic", "It's a trap!", admin);

        final ConfluenceUser addonUser = getAddonUser();

        /*
         * Confluence caches some security stuff on thread local and due to a bug we need to blast it away before checking permission
         */
        ThreadLocalCache.flush();

        boolean addonCanAdministerNewSpace = spacePermissionManager.hasPermission(ADMINISTER_SPACE_PERMISSION, jediSpace, addonUser);
        assertTrue("Add-on user " + getAddonUsername(plugin) + " should have administer permission for space " + jediSpace.getKey(), addonCanAdministerNewSpace);
    }

    @Test
    public void isNotSpaceAdminAfterDowngrade() throws Exception
    {
        plugin = installPlugin(ScopeName.DELETE);
        assertEquals(false, isUserSpaceAdminOfAnySpace(getAddonUsername(plugin)));
    }

    @Test
    public void isNotSpaceAdminAfterUpgrade() throws Exception
    {
        plugin = installPlugin(ScopeName.ADMIN);
        assertEquals(false, isUserSpaceAdminOfAnySpace(getAddonUsername(plugin)));
    }

    private boolean isUserSpaceAdminOfAnySpace(String username)
    {
        // now flush the permissions cache so that it rebuilds to reflect new permission sets
        //
        // this is needed because Confluence's CachingSpacePermissionManager caches permissions in ThreadLocalCache
        // and doesn't realise when the permissions have changed
        //
        // the alternative is to flush the cache in the prod code, which may have unintended side-effects
        ThreadLocalCache.flush();

        final ConfluenceUser addonUser = getUser(username);

        /*
         * is an admin if admin on any of the spaces
         */
        List<Space> allSpaces = spaceManager.getAllSpaces();
        return Iterables.any(allSpaces, new Predicate<Space>()
        {
            @Override
            public boolean apply(@Nullable Space space)
            {
                ThreadLocalCache.flush(); // !!!!!!!!!!!!!!!!!!
                final boolean hasPermission = spacePermissionManager.hasPermission(ADMINISTER_SPACE_PERMISSION, space, addonUser);
                return hasPermission;
            }
        });
    }

    private ConfluenceUser getAddonUser()
    {
        return getUser(getAddonUsername(plugin));
    }

    private ConfluenceUser getUser(String username)
    {
        return FindUserHelper.getUserByUsername(username);
    }

    private String getAddonUsername(Plugin plugin)
    {
        checkArgument(null != plugin, "'plugin' must not be null!");
        ApplicationLink appLink = jwtApplinkFinder.find(plugin.getKey());
        return (String) appLink.getProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME);
    }

    private Plugin installPlugin(ScopeName scope) throws IOException
    {
        String key = "ac-test-" + System.currentTimeMillis();
        ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
                .withKey(key)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withScopes(ImmutableSet.of(scope))
                .build();

        log.warn("Installing test plugin '{}'", addonBean.getKey());
        Plugin installedPlugin = testPluginInstaller.installPlugin(addonBean);
        checkArgument(null != installedPlugin, "'installedPlugin' should not be null after installation: check the logs for installation messages");
        return installedPlugin;
    }
}
