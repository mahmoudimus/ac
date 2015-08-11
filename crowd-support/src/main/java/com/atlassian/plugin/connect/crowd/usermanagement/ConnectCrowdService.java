package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.spi.user.ConnectAddOnUserDisableException;

import com.google.common.annotations.VisibleForTesting;

public interface ConnectCrowdService
        extends ConnectAddOnUserGroupProvisioningService
{
    User createOrEnableUser(String username, String displayName,
            String emailAddress, PasswordCredential passwordCredential);

    User createOrEnableUser(String username, String displayName,
            String emailAddress, PasswordCredential passwordCredential, Map<String, Set<String>> attributes);

    void disableUser(String username) throws ConnectAddOnUserDisableException;

    @VisibleForTesting
    boolean isUserActive(String username);
}
