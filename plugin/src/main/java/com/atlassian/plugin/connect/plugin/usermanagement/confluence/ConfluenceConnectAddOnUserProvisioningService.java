package com.atlassian.plugin.connect.plugin.usermanagement.confluence;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.confluence.security.SpacePermission.ADMINISTER_SPACE_PERMISSION;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.Builder;

@SuppressWarnings ("unused")
@ConfluenceComponent
@ExportAsDevService
public class ConfluenceConnectAddOnUserProvisioningService implements ConnectAddOnUserProvisioningService
{
    private static final ImmutableSet<String> GROUPS = ImmutableSet.of("confluence-users");

    private final SpacePermissionManager spacePermissionManager;
    private final SpaceManager spaceManager;
    private final UserAccessor userAccessor;
    private final UserManager userManager;

    @Autowired
    public ConfluenceConnectAddOnUserProvisioningService(SpacePermissionManager spacePermissionManager, SpaceManager spaceManager,
            UserAccessor userAccessor, UserManager userManager)
    {
        this.spacePermissionManager = spacePermissionManager;
        this.spaceManager = spaceManager;
        this.userAccessor = userAccessor;
        this.userManager = userManager;
    }

    @Override
    public void provisionAddonUserForScopes(String userKey, Collection<ScopeName> scopes)
    {
        provisionAddonUserInSpacesForScopes(userKey, scopes);
    }

    @Override
    public Set<String> getDefaultProductGroups()
    {
        // As reported by Sam Day, without the "confluence-users" group the add-on user can't
        // even get the page summary of a page that is open to anonymous access.
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

    private void provisionAddonUserInSpacesForScopes(String userKey, Collection<ScopeName> scopes)
    {
        final List<Space> spaces = spaceManager.getAllSpaces();
        for (Space space : spaces)
        {
            provisionAddonUserInSpaceForScopes(userKey, scopes, space);
        }
    }

    private void provisionAddonUserInSpaceForScopes(String userKey, Collection<ScopeName> scopes, Space space)
    {
        final Set<String> permissions = getSpacePermissionsImpliedBy(scopes);
        final ConfluenceUser user = getConfluenceUser(userKey);
        for (String permissionType : permissions)
        {
            final SpacePermission spacePermission = new SpacePermission(permissionType, space, null, user);
            if (!spacePermissionManager.hasPermission(permissionType, space, user))
            {
                spacePermissionManager.savePermission(spacePermission);
            }
        }
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

    private Set<String> getSpacePermissionsImpliedBy(Collection<ScopeName> scopes)
    {
        final Builder<String> builder = ImmutableSet.builder();
        for (ScopeName scope : scopes)
        {
            builder.addAll(getSpacePermissionsImpliedBy(scope));
        }
        return builder.build();
    }

    private Set<String> getSpacePermissionsImpliedBy(ScopeName scope)
    {
        if (scope == ScopeName.SPACE_ADMIN)
        {
            return ImmutableSet.of(ADMINISTER_SPACE_PERMISSION);
        }

        final Builder<String> builder = ImmutableSet.builder();
        for (ScopeName implied : scope.getImplied())
        {
            builder.addAll(getSpacePermissionsImpliedBy(implied));
        }
        return builder.build();
    }
}
