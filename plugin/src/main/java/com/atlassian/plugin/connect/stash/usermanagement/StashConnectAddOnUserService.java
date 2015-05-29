package com.atlassian.plugin.connect.stash.usermanagement;

import java.util.Set;

import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserDisableException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.stash.user.*;

import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsDevService
@StashComponent
public class StashConnectAddOnUserService implements ConnectAddOnUserService
{
    private final UserAdminService userAdminService;
    private final UserService userService;

    @Autowired
    public StashConnectAddOnUserService(UserAdminService userAdminService, UserService userService)
    {
        this.userAdminService = userAdminService;
        this.userService = userService;
    }

    @Override
    public String getOrCreateUserKey(String addOnKey, String addOnDisplayName) throws ConnectAddOnUserInitException
    {
        return String.valueOf(getUser(addOnKey, addOnDisplayName).getId());
    }

    @Override
    public void disableAddonUser(String addOnKey) throws ConnectAddOnUserDisableException {
        ServiceUser user = userService.getServiceUserByName(addOnKey, true);
        if (user == null) {
            throw new ConnectAddOnUserDisableException("No user exists for add-on " + addOnKey);
        }

        if (user.isActive())
        {
            userAdminService.updateServiceUser(new ServiceUserUpdateRequest.Builder(user).active(false).build());
        }
    }

    @Override
    public String provisionAddonUserForScopes(String addOnKey, String addOnDisplayName, Set<ScopeName> previousScopes, Set<ScopeName> newScopes)
            throws ConnectAddOnUserInitException
    {
        // no scope support just yet
        return getOrCreateUserKey(checkNotNull(addOnKey, "addOnKey"), checkNotNull(addOnDisplayName, "addOnDisplayName"));
    }

    @Override
    public boolean isAddOnUserActive(String addOnKey)
    {
        return true;
    }

    private StashUser getUser(String addOnKey, String displayName)
    {
        StashUser user = userService.getServiceUserByName(addOnKey, true);
        if (user == null)
        {
            user = userAdminService.createServiceUser(new ServiceUserCreateRequest.Builder()
                    .label("connect-add-on")
                    .name(addOnKey)
                    .displayName(displayName)
                    .build());
        }
        return user;
    }
}
