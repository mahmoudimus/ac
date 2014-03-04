package com.atlassian.plugin.connect.plugin.installer;

import java.util.List;

import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScope;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableSet;

import static com.atlassian.confluence.security.SpacePermission.ADMINISTER_SPACE_PERMISSION;

@SuppressWarnings("unused")
@ConfluenceComponent
public class ConfluenceConnectAddOnUserProvisioningService implements ConnectAddOnUserProvisioningService
{
    private final SpacePermissionManager spacePermissionManager;
    private final SpaceManager spaceManager;
    private final UserAccessor userAccessor;
    private final UserManager userManager;

    public ConfluenceConnectAddOnUserProvisioningService(SpacePermissionManager spacePermissionManager, SpaceManager spaceManager,
                                                         UserAccessor userAccessor, UserManager userManager)
    {
        this.spacePermissionManager = spacePermissionManager;
        this.spaceManager = spaceManager;
        this.userAccessor = userAccessor;
        this.userManager = userManager;
    }

    @Override
    public void provisionAddonUserForScope(String userKey, AddOnScope scope)
    {
        provisionAddonUserInSpacesForScope(userKey, scope);
    }

    private void provisionAddonUserInSpacesForScope(String userKey, AddOnScope scope)
    {
        final List<Space> spaces = spaceManager.getAllSpaces();
        for (Space space : spaces)
        {
            provisionAddonUserInSpaceForScope(userKey, scope, space);
        }
    }

    private void provisionAddonUserInSpaceForScope(String userKey, AddOnScope scope, Space space)
    {
        final Iterable<String> permissions = getSpacePermissionsImpliedBy(scope);
        for (String permission : permissions)
        {
            final SpacePermission spacePermission = new SpacePermission(permission, space, null, getConfluenceUser(userKey));
            spacePermissionManager.savePermission(spacePermission);
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

    private Iterable<String> getSpacePermissionsImpliedBy(AddOnScope scope)
    {
        return ImmutableSet.of(ADMINISTER_SPACE_PERMISSION); // TODO: actual mapping
    }
}
