package com.atlassian.plugin.connect.plugin.installer;

import java.util.Collection;
import java.util.Set;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.google.common.annotations.VisibleForTesting;

public interface ConnectAddOnUserService
{
    String getOrCreateUserKey(String addOnKey, Set<ScopeName> scopes) throws ConnectAddOnUserInitException;

    void disableAddonUser(String addOnKey) throws ConnectAddOnUserDisableException;
    
    @VisibleForTesting
    boolean isAddOnUserActive(String addOnKey);
}
