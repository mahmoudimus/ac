package com.atlassian.plugin.connect.plugin.installer;


import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

import java.util.Collection;

public interface ConnectAddOnUserProvisioningService
{
    void provisionAddonUserForScopes(String userKey, Collection<ScopeName> scopes);
}
