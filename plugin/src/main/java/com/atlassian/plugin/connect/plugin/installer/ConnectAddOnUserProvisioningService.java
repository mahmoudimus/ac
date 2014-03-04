package com.atlassian.plugin.connect.plugin.installer;


import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

import java.util.Set;

public interface ConnectAddOnUserProvisioningService
{
    void provisionAddonUserForScopes(String userKey, Set<ScopeName> scopes);
}
