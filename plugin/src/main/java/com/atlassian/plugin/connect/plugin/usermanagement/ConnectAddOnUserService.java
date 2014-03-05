package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.google.common.annotations.VisibleForTesting;

import java.util.Set;

public interface ConnectAddOnUserService
{
    String getOrCreateUserKey(String addOnKey) throws ConnectAddOnUserInitException;

    void disableAddonUser(String addOnKey) throws ConnectAddOnUserDisableException;

    String provisionAddonUserForScopes(String addOnKey, Set<ScopeName> previousScopes, Set<ScopeName> newScopes) throws ConnectAddOnUserInitException;

    @VisibleForTesting
    boolean isAddOnUserActive(String addOnKey);
}
