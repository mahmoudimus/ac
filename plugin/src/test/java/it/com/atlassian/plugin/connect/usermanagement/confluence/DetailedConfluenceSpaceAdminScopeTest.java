package it.com.atlassian.plugin.connect.usermanagement.confluence;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.confluence.cache.ThreadLocalCache;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserProvisioningService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String CROWD_APPLICATION_NAME = "crowd-embedded"; // magic knowledge

    private final SpaceManager spaceManager;
    private final SpacePermissionManager spacePermissionManager;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final JwtApplinkFinder jwtApplinkFinder;
    private final ApplicationService applicationService;
    private final ApplicationManager applicationManager;
    private final ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService;
    private Space jediSpace;
    private Plugin plugin;

    public DetailedConfluenceSpaceAdminScopeTest(SpaceManager spaceManager,
                                                 SpacePermissionManager spacePermissionManager,
                                                 TestPluginInstaller testPluginInstaller,
                                                 TestAuthenticator testAuthenticator,
                                                 JwtApplinkFinder jwtApplinkFinder,
                                                 ApplicationService applicationService,
                                                 ApplicationManager applicationManager,
                                                 ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService)
    {
        this.spaceManager = spaceManager;
        this.spacePermissionManager = spacePermissionManager;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.jwtApplinkFinder = jwtApplinkFinder;
        this.applicationService = applicationService;
        this.applicationManager = applicationManager;
        this.connectAddOnUserProvisioningService = connectAddOnUserProvisioningService;
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
        assertIsSpaceAdminOnAllSpaces(getAddonUser());
    }

    @Test
    public void addonIsMadeAdminOfExistingSpaceAfterUpgradeToTopAdmin() throws Exception
    {
        plugin = installPlugin(ScopeName.ADMIN);
        assertIsSpaceAdminOnAllSpaces(getAddonUser());
    }

    @Test
    public void addonIsMadeAdminOfExistingSpaceAfterDowngradeFromTopAdmin() throws Exception
    {
        plugin = installPlugin(ScopeName.ADMIN);
        plugin = installPlugin(ScopeName.SPACE_ADMIN);
        assertIsSpaceAdminOnAllSpaces(getAddonUser());
    }

    private void assertIsSpaceAdminOnAllSpaces(ConfluenceUser addonUser)
    {
        List<String> permissionErrors = checkIsSpaceAdminOnAllSpaces(addonUser, true);
        assertTrue(StringUtils.join(permissionErrors, '\n'), permissionErrors.isEmpty());
    }

    private  List<String> checkIsSpaceAdminOnAllSpaces(ConfluenceUser addonUser, boolean shouldHavePermission)
    {
        List<Space> allSpaces = spaceManager.getAllSpaces();

        List<String> permissionErrors = Lists.newArrayList();

        for (Space space : allSpaces)
        {
            permissionErrors.addAll(checkIsSpaceAdminOnSpace(space, addonUser, shouldHavePermission));
        }

        return permissionErrors;
    }

    private List<String> checkIsSpaceAdminOnSpace(Space space, ConfluenceUser addonUser, boolean shouldHavePermission)
    {
        List<String> permissionErrors = Lists.newArrayList();
        checkHasPermissionOnSpace(permissionErrors, CREATEEDIT_PAGE_PERMISSION, space, addonUser, shouldHavePermission);
        checkHasPermissionOnSpace(permissionErrors, CREATE_ATTACHMENT_PERMISSION, space, addonUser, shouldHavePermission);
        checkHasPermissionOnSpace(permissionErrors, COMMENT_PERMISSION, space, addonUser, shouldHavePermission);
        checkHasPermissionOnSpace(permissionErrors, EDITBLOG_PERMISSION, space, addonUser, shouldHavePermission);
        checkHasPermissionOnSpace(permissionErrors, REMOVE_PAGE_PERMISSION, space, addonUser, shouldHavePermission);
        checkHasPermissionOnSpace(permissionErrors, REMOVE_ATTACHMENT_PERMISSION, space, addonUser, shouldHavePermission);
        checkHasPermissionOnSpace(permissionErrors, REMOVE_COMMENT_PERMISSION, space, addonUser, shouldHavePermission);
        checkHasPermissionOnSpace(permissionErrors, REMOVE_BLOG_PERMISSION, space, addonUser, shouldHavePermission);
        checkHasPermissionOnSpace(permissionErrors, ADMINISTER_SPACE_PERMISSION, space, addonUser, shouldHavePermission);
        return permissionErrors;
    }

    private void checkHasPermissionOnSpace(List<String> permissionErrors, String permission, Space space,
                                           ConfluenceUser addonUser, boolean shouldHavePermission)
    {
        String permissionError = checkHasPermissionOnSpace(permission, space, addonUser, shouldHavePermission);
        if (permissionError != null)
        {
            permissionErrors.add(permissionError);
        }
    }

    private String checkHasPermissionOnSpace(String permission, Space space, ConfluenceUser addonUser, boolean shouldHavePermission)
    {
            /*
             * Confluence caches some security stuff on thread local and due to a bug we need to blast it away before checking permission
             */
            ThreadLocalCache.flush();

        boolean hasPermission = spacePermissionManager.hasPermission(permission, space, addonUser);
        if (hasPermission != shouldHavePermission)
            {
            return "Add-on user " + addonUser.getName() + " should "  + (shouldHavePermission ? "" : "NOT ") + "have "
                    + permission + " permission for space " + space.getKey();
            }
        return null;
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

        final List<String> permissionErrors = checkIsSpaceAdminOnSpace(jediSpace, addonUser, true);

        assertTrue(StringUtils.join(permissionErrors, '\n'), permissionErrors.isEmpty());
    }

    @Test
    public void isNotSpaceAdminAfterDowngrade() throws Exception
    {
        plugin = installPlugin(ScopeName.READ);
        assertNonReadPermissionsRemoved();
    }

    @Test
    public void isNotSpaceAdminAfterDowngradeFromAdmin() throws Exception
    {
        plugin = installPlugin(ScopeName.ADMIN);
        plugin = installPlugin(ScopeName.READ);
        assertNonReadPermissionsRemoved();
    }

    private void assertNonReadPermissionsRemoved() throws UserNotFoundException, ApplicationPermissionException, OperationFailedException, GroupNotFoundException, ApplicationNotFoundException
    {
        final List<String> permissionErrors = checkIsSpaceAdminOnAllSpaces(getAddonUserRemovedFromGroups(), false);
        assertEquals(StringUtils.join(permissionErrors, '\n'), true, permissionErrors.isEmpty());
    }

    private ConfluenceUser getAddonUserRemovedFromGroups()
        {
        final ConfluenceUser addonUser = getAddonUser();
        final Set<String> groups = connectAddOnUserProvisioningService.getDefaultProductGroups();
        for (String group : groups)
            {
            removeUserFromGroup(addonUser.getName(), group);
            }

        return addonUser;
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

    // Richard Atkins says that the Application is immutable and therefore the instance replaced every time changes occur,
    // and that therefore we should never cache it
    private com.atlassian.crowd.model.application.Application getApplication() throws ApplicationNotFoundException
    {
        return applicationManager.findByName(CROWD_APPLICATION_NAME);
    }

    private void removeUserFromGroup(String userKey, String groupKey)
    {
        try
        {
            applicationService.removeUserFromGroup(getApplication(), userKey, groupKey);
            log.info("Removed user '{}' from group '{}'.", userKey, groupKey);
        }
        catch (Exception e)
        {
            log.debug("Failed to removed user from group '" + groupKey + "' . Note user may not have been in group", e);
        }
    }
}
