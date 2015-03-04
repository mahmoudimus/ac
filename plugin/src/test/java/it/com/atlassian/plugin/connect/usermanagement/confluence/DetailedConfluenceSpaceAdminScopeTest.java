package it.com.atlassian.plugin.connect.usermanagement.confluence;

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
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.atlassian.confluence.security.SpacePermission.ADMINISTER_SPACE_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.COMMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.CREATEEDIT_PAGE_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.CREATE_ATTACHMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.EDITBLOG_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_ATTACHMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_BLOG_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_COMMENT_PERMISSION;
import static com.atlassian.confluence.security.SpacePermission.REMOVE_PAGE_PERMISSION;
import static com.atlassian.plugin.connect.test.util.AddonUtil.randomWebItemBean;
import static com.google.common.base.Preconditions.checkNotNull;
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

    private List<Plugin> installedAddonPlugins;

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
        installedAddonPlugins = Lists.newArrayList();
        testAuthenticator.authenticateUser("admin");
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
                log.error("Could not delete space", e);
            }
        }

        for (Plugin plugin : installedAddonPlugins)
        {
            if (plugin != null)
            {
                try
                {
                    testPluginInstaller.uninstallAddon(plugin);
                }
                catch (Exception e)
                {
                    log.error("Could not uninstall add-on", e);
                }
            }
        }
    }

    @Test
    public void addonIsMadeAdminOfExistingSpace() throws Exception
    {
        ConnectAddonBean addonBean = createAddonBean(ScopeName.SPACE_ADMIN).build();
        installConnectAddon(addonBean);
        assertIsSpaceAdminOnAllSpaces(addonBean.getKey());
    }

    @Test
    public void addonIsMadeAdminOfExistingSpaceAfterUpgradeToTopAdmin() throws Exception
    {
        ConnectAddonBean addonBean = createAddonBean(ScopeName.ADMIN).build();
        installConnectAddon(addonBean);
        assertIsSpaceAdminOnAllSpaces(addonBean.getKey());
    }

    @Test
    public void addonIsMadeAdminOfExistingSpaceAfterDowngradeFromTopAdmin() throws Exception
    {
        ConnectAddonBeanBuilder addonBeanBuilder = createAddonBean(ScopeName.ADMIN);
        String key = addonBeanBuilder.getKey();

        installAddonThenChangeScope(addonBeanBuilder, ScopeName.SPACE_ADMIN);
        assertIsSpaceAdminOnAllSpaces(key);
    }

    @Test
    public void addonIsMadeAdminOfNewSpaceAfterDowngradeFromTopAdmin() throws Exception
    {
        ConnectAddonBeanBuilder addonBeanBuilder = createAddonBean(ScopeName.ADMIN);
        String key = addonBeanBuilder.getKey();

        installAddonThenChangeScope(addonBeanBuilder, ScopeName.SPACE_ADMIN);

        assertIsSpaceAdminOfNewSpace(key);
    }

    @Test
    public void addonWithSpaceAdminIsMadeAdminOfNewSpace() throws Exception
    {
        ConnectAddonBeanBuilder addonBeanBuilder = createAddonBean(ScopeName.SPACE_ADMIN);
        String key = addonBeanBuilder.getKey();

        installConnectAddon(addonBeanBuilder.build());
        assertIsSpaceAdminOfNewSpace(key);
    }

    @Test
    public void isNotSpaceAdminAfterDowngradeFromSpaceAdmin() throws Exception
    {
        ConnectAddonBeanBuilder addonBeanBuilder = createAddonBean(ScopeName.SPACE_ADMIN);
        String key = addonBeanBuilder.getKey();

        installAddonThenChangeScope(addonBeanBuilder, ScopeName.READ);

        assertNonReadPermissionsRemoved(key);
    }

    @Test
    public void isNotSpaceAdminAfterDowngradeFromAdmin() throws Exception
    {
        ConnectAddonBeanBuilder addonBeanBuilder = createAddonBean(ScopeName.ADMIN);
        String key = addonBeanBuilder.getKey();

        installAddonThenChangeScope(addonBeanBuilder, ScopeName.READ);

        assertNonReadPermissionsRemoved(key);
    }

    private void installAddonThenChangeScope(ConnectAddonBeanBuilder addonBeanBuilder, ScopeName upgradedScope)
            throws Exception
    {
        installConnectAddon(addonBeanBuilder.build());

        addonBeanBuilder.withScopes(ImmutableSet.of(upgradedScope));
        // install add-on with new scopes
        installConnectAddon(addonBeanBuilder.build());
    }

    private void assertIsSpaceAdminOnAllSpaces(String addonKey)
    {
        ConfluenceUser addonUser = getAddonUser(addonKey);
        List<String> permissionErrors = checkIsSpaceAdminOnAllSpaces(addonUser, true);
        assertTrue(StringUtils.join(permissionErrors, '\n'), permissionErrors.isEmpty());
    }

    private List<String> checkIsSpaceAdminOnAllSpaces(ConfluenceUser addonUser, boolean shouldHavePermission)
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
            return "Add-on user " + addonUser.getName() + " should " + (shouldHavePermission ? "" : "NOT ") + "have "
                    + permission + " permission for space " + space.getKey();
        }
        return null;
    }


    private void assertIsSpaceAdminOfNewSpace(String addonKey)
    {
        ConfluenceUser admin = FindUserHelper.getUserByUsername("admin");

        jediSpace = spaceManager.createSpace(JEDI_SPACE_KEY, "Knights of the Old Republic", "It's a trap!", admin);

        final ConfluenceUser addonUser = getAddonUser(addonKey);

        final List<String> permissionErrors = checkIsSpaceAdminOnSpace(jediSpace, addonUser, true);

        assertTrue(StringUtils.join(permissionErrors, '\n'), permissionErrors.isEmpty());
    }

    private void assertNonReadPermissionsRemoved(String addonKey)
            throws UserNotFoundException, ApplicationPermissionException, OperationFailedException, GroupNotFoundException, ApplicationNotFoundException
    {
        final List<String> permissionErrors = checkIsSpaceAdminOnAllSpaces(getAddonUserRemovedFromGroups(addonKey), false);
        assertEquals(StringUtils.join(permissionErrors, '\n'), true, permissionErrors.isEmpty());
    }

    private ConfluenceUser getAddonUserRemovedFromGroups(String addonKey)
    {
        final ConfluenceUser addonUser = getAddonUser(addonKey);
        final Set<String> groups = connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected();
        groups.addAll(connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected());
        for (String group : groups)
        {
            removeUserFromGroup(addonUser.getName(), group);
        }

        return addonUser;
    }

    private ConfluenceUser getAddonUser(String addonKey)
    {
        return getUser(getAddonUsername(addonKey));
    }

    private ConfluenceUser getUser(String username)
    {
        return FindUserHelper.getUserByUsername(username);
    }

    private String getAddonUsername(String addonKey)
    {
        checkNotNull(addonKey, "addonKey must not be null");
        ApplicationLink appLink = jwtApplinkFinder.find(addonKey);
        return (String) appLink.getProperty(JwtConstants.AppLinks.ADD_ON_USER_KEY_PROPERTY_NAME);
    }

    private ConnectAddonBeanBuilder createAddonBean(ScopeName scope)
    {
        String key = "ac-test-" + System.currentTimeMillis();
        return ConnectAddonBean.newConnectAddonBean()
                .withKey(key)
                .withDescription(getClass().getCanonicalName())
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withModule("webItems", randomWebItemBean())
                .withScopes(ImmutableSet.of(scope));
    }

    private void installConnectAddon(ConnectAddonBean addonBean) throws IOException
    {
        log.warn("Installing test addon '{}'", addonBean.getKey());
        Plugin installedPlugin = testPluginInstaller.installAddon(addonBean);
        checkNotNull(installedPlugin, "'installedPlugin' should not be null after installation: check the logs for installation messages");
        installedAddonPlugins.add(installedPlugin);
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
