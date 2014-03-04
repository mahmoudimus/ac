package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.google.common.annotations.VisibleForTesting;

import java.util.Set;

public interface ConnectAddOnUserService
{
    String getOrCreateUserKey(String addOnKey, Set<ScopeName> scopes) throws ConnectAddOnUserInitException;

    User getUserByAddOnKey(String addOnKey);

    void disableAddonUser(String addOnKey) throws ConnectAddOnUserDisableException;

    @VisibleForTesting
    boolean isAddOnUserActive(String addOnKey);
}
