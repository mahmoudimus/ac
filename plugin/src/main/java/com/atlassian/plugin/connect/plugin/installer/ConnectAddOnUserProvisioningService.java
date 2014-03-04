package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.plugin.scopes.AddOnScope;

public interface ConnectAddOnUserProvisioningService
{
    void provisionAddonUserForScope(String userKey, AddOnScope scope);
}
