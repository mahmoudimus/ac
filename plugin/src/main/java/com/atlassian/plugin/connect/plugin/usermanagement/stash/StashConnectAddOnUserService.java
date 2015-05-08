package com.atlassian.plugin.connect.plugin.usermanagement.stash;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserDisableException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserAdminService;
import com.atlassian.stash.user.UserService;

import com.google.common.collect.Maps;

import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsDevService
@StashComponent
public class StashConnectAddOnUserService implements ConnectAddOnUserService
{
    private final UserAdminService userAdminService;
    private final UserService userService;
    // TODO: replace this with a more persistent storage
    private final ConcurrentMap<String, Integer> nameToUserId;

    @Autowired
    public StashConnectAddOnUserService(UserAdminService userAdminService, UserService userService)
    {
        this.userAdminService = userAdminService;
        this.userService = userService;

        nameToUserId = Maps.newConcurrentMap();
    }

    @Override
    public String getOrCreateUserKey(String addOnKey, String addOnDisplayName) throws ConnectAddOnUserInitException
    {
        return String.valueOf(getUser(addOnKey, addOnDisplayName).getId());
    }

    @Override
    public void disableAddonUser(String addOnKey) throws ConnectAddOnUserDisableException
    {
        // not supported yet
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
        StashUser user = null;
        while (user == null)
        {
            Integer userId = nameToUserId.get(addOnKey);
            if (userId == null)
            {
                user = userAdminService.createServiceUser(displayName);
                userId = nameToUserId.putIfAbsent(addOnKey, user.getId());
                if (userId != null)
                {
                    // user has been created concurrently - use that instead
                    user = null;
                }
            }
            else
            {
                user = userService.getUserById(userId);
                if (user == null)
                {
                    nameToUserId.remove(addOnKey);
                }
            }
        }
        return user;
    }
}
