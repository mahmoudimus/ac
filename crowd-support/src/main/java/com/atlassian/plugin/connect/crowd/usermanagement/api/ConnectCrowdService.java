package com.atlassian.plugin.connect.crowd.usermanagement.api;

import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.spi.user.ConnectAddOnUserDisableException;

public interface ConnectCrowdService
        extends ConnectAddOnUserGroupProvisioningService
{
    User createOrEnableUser(String username, String displayName,
            String emailAddress, PasswordCredential passwordCredential);

    void disableUser(String username) throws ConnectAddOnUserDisableException;

    void setAttributesOnUser(User user, Map<String, Set<String>> attributes);

}
