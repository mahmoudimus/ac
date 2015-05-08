package com.atlassian.plugin.connect.stash.usermanagement;

import java.util.Set;

import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.user.ConnectUserService;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.stash.user.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

@ExportAsDevService
@StashComponent
public class StashConnectUserService implements ConnectUserService
{
    private final UserAdminService userAdminService;
    private final UserManager userManager;
    private final UserService userService;

    @Autowired
    public StashConnectUserService(UserAdminService userAdminService, UserManager userManager, UserService userService)
    {
        this.userAdminService = userAdminService;
        this.userManager = userManager;
        this.userService = userService;
    }

    @Nonnull
    @Override
    public UserProfile getOrCreateAddonUser(@Nonnull String addonKey, @Nonnull String addonDisplayName) throws ConnectAddOnUserInitException {
        return toUserProfile(getOrCreateUser(addonKey, addonDisplayName));
    }

    @Override
    public boolean isUserActive(@Nonnull UserProfile userProfile) {
        StashUser user = getUserByKey(userProfile.getUserKey());
        return user != null && user.isActive();
    }

    @Override
    public void setAddonUserActive(@Nonnull String addonKey, final boolean active)
    {
        StashUser user = getAddonUser(addonKey);
        if (user == null)
        {
            throw new IllegalStateException("No user exists for add-on " + addonKey);
        }
        if (user.isActive() != active)
        {
            if (user.getType() != UserType.SERVICE)
            {
                throw new IllegalStateException("Expected a service user, but got " + user.getType());
            }
            userAdminService.updateServiceUser(new ServiceUserUpdateRequest.Builder((ServiceUser) user).active(active).build());
        }
    }

    @Nonnull
    @Override
    public UserProfile provisionAddonUserForScopes(@Nonnull String addonKey, @Nonnull String addonDisplayName,
                                      @Nonnull Set<ScopeName> previousScopes, @Nonnull Set<ScopeName> newScopes) throws ConnectAddOnUserInitException {
        // just provision the user - no special actions required to enforce scopes at this time
        return toUserProfile(getOrCreateUser(addonKey, addonDisplayName));
    }

    private StashUser getAddonUser(String addOnKey) {
        return userService.getServiceUserByName(getAddonUsername(addOnKey), true);
    }

    private String getAddonUsername(String addonKey) {
        return ConnectAddOnUserUtil.usernameForAddon(addonKey);
    }

    private StashUser getOrCreateUser(String addOnKey, String displayName)
    {
        String username = getAddonUsername(addOnKey);
        ServiceUser user = userService.getServiceUserByName(username, true);
        if (user == null)
        {
            user = userAdminService.createServiceUser(new ServiceUserCreateRequest.Builder()
                    .label("connect-add-on")
                    .name(username)
                    .displayName(displayName)
                    .build());
        }
        if (!user.isActive())
        {
            userAdminService.updateServiceUser(new ServiceUserUpdateRequest.Builder(user)
                .active(true)
                .build());
        }
        return user;
    }

    private StashUser getUserByKey(@Nonnull UserKey userKey)
    {
        StashUser user = null;
        String userKeyString = userKey.getStringValue();
        if (StringUtils.isNumeric(userKey.getStringValue()))
        {
            try
            {
                user = userService.getUserById(Integer.parseInt(userKeyString), true);
            } catch (NumberFormatException e)
            {
                // ignore
            }
        }
        if (user == null)
        {
            user = userService.getUserByName(userKeyString, true);
        }
        if (user == null)
        {
            user = userService.getServiceUserByName(userKeyString, true);
        }
        return user;
    }

    private UserKey toUserKey(StashUser user)
    {
        return UserKey.fromLong(user.getId());
    }

    private UserProfile toUserProfile(StashUser user)
    {
        return userManager.getUserProfile(toUserKey(user));
    }
}
