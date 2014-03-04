package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.google.common.annotations.VisibleForTesting;

import java.util.Set;

public interface ConnectAddOnUserService
{
    String getOrCreateUserKey(String addOnKey, Set<ScopeName> scopes) throws ConnectAddOnUserInitException;
    void disableAddonUser(String addOnKey) throws ConnectAddOnUserDisableException;
    
    @VisibleForTesting
    boolean isAddOnUserActive(String addOnKey);
}
