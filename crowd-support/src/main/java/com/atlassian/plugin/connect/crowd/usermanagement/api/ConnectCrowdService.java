package com.atlassian.plugin.connect.crowd.usermanagement.api;

import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.connect.spi.user.ConnectAddOnUserDisableException;

public interface ConnectCrowdService
{
    User createOrEnableUser(String username, String displayName,
            String emailAddress, PasswordCredential passwordCredential);

    void disableUser(String username) throws ConnectAddOnUserDisableException;

    void setAttributesOnUser(User user, Map<String, Set<String>> attributes);

    boolean isUserDirectGroupMember(String userName, String groupName);

    void addUserToGroup(String userName, String groupName);

    void removeUserFromGroup(String userName, String groupName);

    void addGroup(String groupName);

    Group getGroup(String groupName);
}
