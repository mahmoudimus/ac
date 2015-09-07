package com.atlassian.plugin.connect.bitbucket.usermanagement;

import java.util.Set;

import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.user.ConnectAddOnUserDisableException;
import com.atlassian.plugin.connect.spi.user.ConnectUserService;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.bitbucket.user.*;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

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
    public void disableAddOnUser(String addOnKey) throws ConnectAddOnUserDisableException
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

    @Override
    public String getOrCreateAddOnUserName(String addOnKey, String addOnDisplayName) throws ConnectAddOnUserInitException
    {
        return getOrCreateUser(addOnKey, addOnDisplayName).getName();
    }

    @Override
    public boolean isActive(String username) {
        ApplicationUser user = userService.getServiceUserByName(username, true);
        if (user == null) {
            user = userService.getUserByName(username, false);
        }
        return user != null && user.isActive();
    }

    @Nonnull
    @Override
    public String provisionAddOnUserForScopes(@Nonnull String addonKey, @Nonnull String addonDisplayName,
                                      @Nonnull Set<ScopeName> previousScopes, @Nonnull Set<ScopeName> newScopes) throws ConnectAddOnUserInitException
    {
        // just provision the user - no special actions required to enforce scopes at this time
        return getOrCreateUser(addonKey, addonDisplayName).getName();
    }

    private ApplicationUser getAddonUser(String addOnKey)
    {
        return userService.getServiceUserByName(getAddonUsername(addOnKey), true);
    }

    private String getAddonUsername(String addonKey)
    {
        return ConnectAddOnUserUtil.usernameForAddon(addonKey);
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
