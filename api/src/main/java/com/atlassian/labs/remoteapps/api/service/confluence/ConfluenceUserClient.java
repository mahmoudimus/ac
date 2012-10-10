package com.atlassian.labs.remoteapps.api.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.domain.MutableUser;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.MutableUserInformation;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.User;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.UserInformation;
import com.atlassian.labs.remoteapps.spi.util.RequirePermission;
import com.atlassian.util.concurrent.Promise;

/**
 */
public interface ConfluenceUserClient
{
    @RequirePermission(ConfluencePermission.READ_USERS_AND_GROUPS)
    Promise<User> getUser(String userName);

    @RequirePermission(ConfluencePermission.MODIFY_USERS)
    Promise<Void> editUser(MutableUser user);

    @RequirePermission(ConfluencePermission.READ_USERS_AND_GROUPS)
    Promise<Iterable<String>> getUserGroups(String username);

    // todo: add permission
    Promise<Boolean> addUser(MutableUser user, String password, boolean notifyUser);
    Promise<Boolean> removeUser(String username);
    Promise<Boolean> addUserToGroup(String userName, String groupName);
    Promise<Boolean> removeUserFromGroup(String userName, String groupName);
    Promise<Void> addGroup(String groupName);
    Promise<Void> removeAllPermissionsForGroup(String groupName);
    Promise<Void> removeGroup(String groupName, String moveToGroupName);
    Promise<Void> deactivateUser(String userName);
    Promise<Void> reactivateUser(String userName);
    Promise<Void> changeMyPassword(String oldPass, String newPass);
    Promise<Void> changeUserPassword(String userName, String newPass);

    @RequirePermission(ConfluencePermission.READ_USERS_AND_GROUPS)
    Promise<Iterable<String>> getGroups();

    @RequirePermission(ConfluencePermission.READ_USERS_AND_GROUPS)
    Promise<Boolean> isActiveUser(String userName);

    @RequirePermission(ConfluencePermission.READ_USERS_AND_GROUPS)
    Promise<Iterable<String>> getActiveUsers(boolean viewAll);

    @RequirePermission(ConfluencePermission.MODIFY_USERS)
    Promise<Void> setUserInformation(MutableUserInformation userInfo);

    @RequirePermission(ConfluencePermission.READ_USERS_AND_GROUPS)
    Promise<UserInformation> getUserInformation(String userName);

    @RequirePermission(ConfluencePermission.MODIFY_USERS)
    Promise<Void> setUserPreferenceBoolean(String userName, String key, boolean value);

    @RequirePermission(ConfluencePermission.READ_USERS_AND_GROUPS)
    Promise<Boolean> getUserPreferenceBoolean(String userName, String key);

    @RequirePermission(ConfluencePermission.MODIFY_USERS)
    Promise<Void> setUserPreferenceLong(String userName, String key, long value);

    @RequirePermission(ConfluencePermission.READ_USERS_AND_GROUPS)
    Promise<Long> getUserPreferenceLong(String userName, String key);

    @RequirePermission(ConfluencePermission.MODIFY_USERS)
    Promise<Void> setUserPreferenceString(String userName, String key, String value);

    @RequirePermission(ConfluencePermission.READ_USERS_AND_GROUPS)
    Promise<String> getUserPreferenceString(String userName, String key);

    @RequirePermission(ConfluencePermission.READ_USERS_AND_GROUPS)
    Promise<Boolean> hasUser(String userName);

    @RequirePermission(ConfluencePermission.READ_USERS_AND_GROUPS)
    Promise<Boolean> hasGroup(String groupName);

    @RequirePermission(ConfluencePermission.MODIFY_USERS)
    Promise<Void> addProfilePicture(String userName, String fileName, String mimeType, byte[] pictureData);
}
