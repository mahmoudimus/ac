package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.plugin.scopes.AddOnScope;

public interface ConnectAddOnUserGroupProvisioningService
{
    void addAddonToGroupsForScope(String userKey, AddOnScope scope);
}
