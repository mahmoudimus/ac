package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonDisableException;
import com.google.common.annotations.VisibleForTesting;

import java.util.Map;
import java.util.Set;

public interface ConnectCrowdService
        extends ConnectAddonUserGroupProvisioningService {
    UserCreationResult createOrEnableUser(String username, String displayName,
                                          String emailAddress, PasswordCredential passwordCredential);

    UserCreationResult createOrEnableUser(String username, String displayName,
                                          String emailAddress, PasswordCredential passwordCredential, Map<String, Set<String>> attributes);

    void disableUser(String username) throws ConnectAddonDisableException;

    @VisibleForTesting
    boolean isUserActive(String username);

    void invalidateSessions(String username) throws OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;
}
