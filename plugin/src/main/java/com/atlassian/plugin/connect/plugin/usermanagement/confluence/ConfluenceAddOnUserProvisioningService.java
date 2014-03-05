package com.atlassian.plugin.connect.plugin.usermanagement.confluence;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserProvisioningService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static com.atlassian.confluence.security.SpacePermission.ADMINISTER_SPACE_PERMISSION;

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
            SpacePermissionManager spacePermissionManager, SpaceManager spaceManager,
            UserAccessor userAccessor, UserManager userManager)
    {
        this.confluencePermissionManager = confluencePermissionManager;
        this.spacePermissionManager = spacePermissionManager;
        this.spaceManager = spaceManager;
        this.userAccessor = userAccessor;
        this.userManager = userManager;
    }

    @Override
    public void provisionAddonUserForScopes(String addonUserKey, Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
    {
        final ConfluenceUser confluenceAddonUser = getConfluenceUser(addonUserKey);
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

    private ConfluenceUser getConfluenceUser(String userKeyString)
    {
        UserProfile userProfile = userManager.getUserProfile(userKeyString);

        if (userProfile == null)
        {
            throw new IllegalStateException("User for user key " + userKeyString + " does not exist");
        }

        return userAccessor.getExistingUserByKey(userProfile.getUserKey());
    }

    private void grantAddonUserGlobalAdmin(ConfluenceUser confluenceAddonUser)
    {
        if (confluencePermissionManager.isConfluenceAdministrator(confluenceAddonUser))
        {
//            throw new UnsupportedOperationException("How do you even set this permission");
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
