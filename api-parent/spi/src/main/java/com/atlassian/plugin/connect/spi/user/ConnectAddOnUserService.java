package com.atlassian.plugin.connect.spi.user;

import java.util.Set;

import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.usermanagment.ConnectAddOnUserDisableException;

public interface ConnectAddOnUserService
{
    String getOrCreateUserName(String addOnKey, String addOnDisplayName) throws ConnectAddOnUserInitException;

    void disableAddonUser(String addOnKey) throws ConnectAddOnUserDisableException;

    String provisionAddonUserForScopes(String addOnKey, String addOnDisplayName, Set<ScopeName> previousScopes, Set<ScopeName> newScopes) throws ConnectAddOnUserInitException;
}
