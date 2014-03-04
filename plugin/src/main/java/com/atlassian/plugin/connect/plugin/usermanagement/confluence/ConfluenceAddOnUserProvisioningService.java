package com.atlassian.plugin.connect.plugin.usermanagement.confluence;

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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.atlassian.confluence.security.SpacePermission.ADMINISTER_SPACE_PERMISSION;
import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings ("unused")
@ConfluenceComponent
@ExportAsDevService
public class ConfluenceAddOnUserProvisioningService implements ConnectAddOnUserProvisioningService
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceAddOnUserProvisioningService.class);

    // As reported by Sam Day, without the "confluence-users" group the add-on user can't
    // even get the page summary of a page that is open to anonymous access.
    private static final ImmutableSet<String> GROUPS = ImmutableSet.of("confluence-users");

    private final SpacePermissionManager spacePermissionManager;
    private final SpaceManager spaceManager;
    private final UserAccessor userAccessor;
    private final UserManager userManager;

    @Autowired
    public ConfluenceAddOnUserProvisioningService(SpacePermissionManager spacePermissionManager, SpaceManager spaceManager,
            UserAccessor userAccessor, UserManager userManager)
    {
        this.spacePermissionManager = spacePermissionManager;
        this.spaceManager = spaceManager;
        this.userAccessor = userAccessor;
        this.userManager = userManager;
    }

    @Override
    public void provisionAddonUserForScopes(String addonUserKey, Collection<ScopeName> scopes)
    {
        checkNotNull(scopes);

        final ConfluenceUser confluenceAddonUser = getConfluenceUser(addonUserKey);

        if (scopes.contains(ScopeName.ADMIN))
        {
            // set global admin permission
        }
        else if (scopes.contains(ScopeName.SPACE_ADMIN))
        {
            // add space admin to all spaces
            final List<Space> spaces = spaceManager.getAllSpaces();
            for (Space space : spaces)
            {
                grantAddonUserSpaceAdmin(space, confluenceAddonUser);
            }
        }
    }

    @Override
    public Set<String> getDefaultProductGroups()
    {
        return GROUPS;
    }

    @Override
    public void ensureGroupHasProductAdminPermission(String groupKey)
    {
        if (!groupHasProductAdminPermission(groupKey))
        {
            throw new UnsupportedOperationException("NIH");
        }
    }

    @Override
    public boolean groupHasProductAdminPermission(String groupKey)
    {
        checkNotNull(groupKey);
        return false;//confluencePermissionManager.
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

    private void grantAddonUserSpaceAdmin(Space space, ConfluenceUser confluenceAddonUser)
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
