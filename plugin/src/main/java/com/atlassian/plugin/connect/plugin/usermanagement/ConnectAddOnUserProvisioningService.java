package com.atlassian.plugin.connect.plugin.usermanagement;

import java.util.Collection;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

public interface ConnectAddOnUserProvisioningService
{
    void provisionAddonUserForScopes(String userKey, Collection<ScopeName> scopes);
}
