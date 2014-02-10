package com.atlassian.plugin.connect.plugin.installer;

import com.google.common.annotations.VisibleForTesting;

public interface ConnectAddOnUserService
{
    String getOrCreateUserKey(String addOnKey) throws ConnectAddOnUserInitException;
    void disableAddonUser(String addOnKey) throws ConnectAddOnUserDisableException;
    
    @VisibleForTesting
    boolean isAddOnUserActive(String addOnKey);
}
