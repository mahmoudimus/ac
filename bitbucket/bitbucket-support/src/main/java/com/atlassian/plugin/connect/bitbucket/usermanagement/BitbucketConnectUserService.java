package com.atlassian.plugin.connect.bitbucket.usermanagement;

import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.ServiceUser;
import com.atlassian.bitbucket.user.ServiceUserCreateRequest;
import com.atlassian.bitbucket.user.ServiceUserUpdateRequest;
import com.atlassian.bitbucket.user.UserAdminService;
import com.atlassian.bitbucket.user.UserService;
import com.atlassian.bitbucket.user.UserType;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonDisableException;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonInitException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.auth.user.ConnectUserService;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.Set;

@ExportAsDevService
@BitbucketComponent
public class BitbucketConnectUserService implements ConnectUserService
{
    private final UserAdminService userAdminService;
    private final UserService userService;

    @Autowired
    public BitbucketConnectUserService(UserAdminService userAdminService, UserService userService)
    {
        this.userAdminService = userAdminService;
        this.userService = userService;
    }

    @Override
    public void disableAddonUser(@Nonnull String addOnKey) throws ConnectAddonDisableException
    {
        ApplicationUser user = getAddonUser(addOnKey);
        if (user == null)
        {
            throw new IllegalStateException("No user exists for add-on " + addOnKey);
        }
        if (user.isActive())
        {
            if (user.getType() != UserType.SERVICE)
            {
                throw new IllegalStateException("Expected a service user, but got " + user.getType());
            }
            userAdminService.updateServiceUser(new ServiceUserUpdateRequest.Builder((ServiceUser) user).active(false).build());
        }
    }

    @Nonnull
    @Override
    public String getOrCreateAddonUserName(@Nonnull String addOnKey, @Nonnull String addOnDisplayName) throws ConnectAddonInitException
    {
        return getOrCreateUser(addOnKey, addOnDisplayName).getName();
    }

    @Override
    public boolean isActive(@Nonnull String username) {
        ApplicationUser user = userService.getServiceUserByName(username, true);
        if (user == null) {
            user = userService.getUserByName(username, false);
        }
        return user != null && user.isActive();
    }

    @Nonnull
    @Override
    public String provisionAddonUserWithScopes(@Nonnull ConnectAddonBean addon,
                                              @Nonnull Set<ScopeName> previousScopes, @Nonnull Set<ScopeName> newScopes) throws ConnectAddonInitException
    {
        // just provision the user - no special actions required to enforce scopes at this time
        return getOrCreateUser(addon.getKey(), addon.getName()).getName();
    }

    private ApplicationUser getAddonUser(String addOnKey)
    {
        return userService.getServiceUserByName(getAddonUsername(addOnKey), true);
    }

    private String getAddonUsername(String addonKey)
    {
        return "addon_" + addonKey;
    }

    private ApplicationUser getOrCreateUser(String addOnKey, String displayName)
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
}
