package com.atlassian.plugin.connect.plugin.usermanagement.confluence;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.security.administrators.EditPermissionsAdministrator;
import com.atlassian.confluence.security.administrators.PermissionsAdministratorBuilder;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserProvisioningService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.confluence.security.SpacePermission.ADMINISTER_SPACE_PERMISSION;
import static java.util.Arrays.asList;

@SuppressWarnings ("unused")
@ConfluenceComponent
@ExportAsDevService
public class ConfluenceAddOnUserProvisioningService implements ConnectAddOnUserProvisioningService
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceAddOnUserProvisioningService.class);

    // As reported by Sam Day, without the "confluence-users" group the add-on user can't
    // even get the page summary of a page that is open to anonymous access.
    private static final ImmutableSet<String> GROUPS = ImmutableSet.of("confluence-users");

    private final PermissionManager confluencePermissionManager;
    private final SpacePermissionManager spacePermissionManager;
    private final SpaceManager spaceManager;
    private final UserAccessor userAccessor;
    private final UserManager userManager;

    @Autowired
    public ConfluenceAddOnUserProvisioningService(PermissionManager confluencePermissionManager,
                                                  SpacePermissionManager spacePermissionManager,
                                                  SpaceManager spaceManager,
                                                  UserAccessor userAccessor,
                                                  UserManager userManager)
    {
        this.confluencePermissionManager = confluencePermissionManager;
        this.spacePermissionManager = spacePermissionManager;
        this.spaceManager = spaceManager;
        this.userAccessor = userAccessor;
        this.userManager = userManager;
    }

    @Override
    public void provisionAddonUserForScopes(String username, Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        final ConfluenceUser confluenceAddonUser = getConfluenceUser(username);
        Set<ScopeName> previousScopesNormalized = ScopeName.normalize(previousScopes);
        Set<ScopeName> newScopesNormalized = ScopeName.normalize(newScopes);

        if (newScopes.contains(ScopeName.ADMIN))
        {
            grantAddonUserGlobalAdmin(confluenceAddonUser);
        }
        else if (newScopes.contains(ScopeName.SPACE_ADMIN) && !previousScopes.contains(ScopeName.SPACE_ADMIN))
        {
            // add space admin to all spaces
            grantAddonUserSpaceAdmin(confluenceAddonUser);
        }
    }

    @Override
    public Set<String> getDefaultProductGroups()
    {
        return GROUPS;
    }

    private ConfluenceUser getConfluenceUser(String username)
    {
        UserProfile userProfile = userManager.getUserProfile(username);

        if (userProfile == null)
        {
            throw new IllegalStateException("User for user key " + username + " does not exist");
        }

        return userAccessor.getExistingUserByKey(userProfile.getUserKey());
    }

    private void grantAddonUserGlobalAdmin(final ConfluenceUser confluenceAddonUser)
    {
        if (!confluencePermissionManager.isConfluenceAdministrator(confluenceAddonUser))
        {
            // injecting as a dependency results in an instance with null data members - perhaps there are multiple instances or perhaps it is initialized after us
            final PermissionsAdministratorBuilder confluencePermissionsAdministratorBuilder = ComponentLocator.getComponent(PermissionsAdministratorBuilder.class, "permissionsAdministratorBuilder");

            // we need to run this without permissions checking otherwise our attempt to modify permissions will be permissions-checked,
            // and will be accordingly rejected unless we tell Confluence that it's being performed by an arbitrarily selected admin user
            confluencePermissionManager.withExemption(new Runnable()
            {
                @Override
                public void run()
                {
                    EditPermissionsAdministrator confluenceEditPermissionsAdministrator = confluencePermissionsAdministratorBuilder.buildEditGlobalPermissionAdministrator(null, asList(confluenceAddonUser.getName()), Collections.<String>emptyList());
                    confluenceEditPermissionsAdministrator.addPermission(SpacePermission.createUserSpacePermission(SpacePermission.CONFLUENCE_ADMINISTRATOR_PERMISSION, null, confluenceAddonUser));
                }
            });
            // Confluence's CachingSpacePermissionManager caches permissions in ThreadLocalCache and doesn't realise when the permissions have changed.
            // This is Less Than Ideal.
            // Due to this bug all threads now have an outdated copy of this user's admin permission.
            // On a new request from the add-on or page-view by a user a new thread will be used, so they will be OK.
            // We could call ThreadLocalCache.flush() to fix up this thread but that may have unintended side-effects.
            // The test code in ConfluenceAdminScopeTestBase flushes the thread-local cache before checking results.
        }
    }

    private void grantAddonUserSpaceAdmin(ConfluenceUser confluenceAddonUser)
    {
        final List<Space> spaces = spaceManager.getAllSpaces();
        for (Space space : spaces)
        {
            grantAddonUserAdminToSpace(space, confluenceAddonUser);
        }
    }

    private void grantAddonUserAdminToSpace(Space space, ConfluenceUser confluenceAddonUser)
    {
        if (!spacePermissionManager.hasPermission(ADMINISTER_SPACE_PERMISSION, space, confluenceAddonUser))
        {
            SpacePermission permission = new SpacePermission(ADMINISTER_SPACE_PERMISSION, space, null, confluenceAddonUser);
            spacePermissionManager.savePermission(permission);
        }
        else
        {
            log.info("Add-on user {} already has admin permission on space {}", confluenceAddonUser.getName(), space.getKey());
        }
    }
}
