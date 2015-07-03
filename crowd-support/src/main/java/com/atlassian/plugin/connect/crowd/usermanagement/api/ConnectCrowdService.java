package com.atlassian.plugin.connect.crowd.usermanagement.api;

import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;

public interface ConnectCrowdService
{
    User createOrEnableUser(String username, String displayName, String emailAddress, PasswordCredential passwordCredential)
            throws ConnectCrowdException;

    void disableUser(String username) throws ConnectCrowdException;

    void setAttributesOnUser(User user, Map<String, Set<String>> attributes)
            throws ConnectCrowdException;
}
