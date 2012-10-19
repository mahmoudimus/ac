package com.atlassian.plugin.remotable.api.service.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.domain.MutableUser;
import com.atlassian.plugin.remotable.api.service.confluence.domain.MutableUserInformation;
import com.atlassian.plugin.remotable.api.service.confluence.domain.User;
import com.atlassian.plugin.remotable.api.service.confluence.domain.UserInformation;
import com.atlassian.plugin.remotable.spi.util.RequirePermission;
import com.atlassian.util.concurrent.Promise;

/**
 */
public interface ConfluenceUserClient
{
    @RequirePermission(ConfluencePermissions.READ_USERS_AND_GROUPS)
    Promise<User> getUser(String userName);

    @RequirePermission(ConfluencePermissions.MODIFY_USERS)
    Promise<Void> editUser(MutableUser user);

    @RequirePermission(ConfluencePermissions.READ_USERS_AND_GROUPS)
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

    @RequirePermission(ConfluencePermissions.READ_USERS_AND_GROUPS)
    Promise<Iterable<String>> getGroups();

    @RequirePermission(ConfluencePermissions.READ_USERS_AND_GROUPS)
    Promise<Boolean> isActiveUser(String userName);

    @RequirePermission(ConfluencePermissions.READ_USERS_AND_GROUPS)
    Promise<Iterable<String>> getActiveUsers(boolean viewAll);

    @RequirePermission(ConfluencePermissions.MODIFY_USERS)
    Promise<Void> setUserInformation(MutableUserInformation userInfo);

    @RequirePermission(ConfluencePermissions.READ_USERS_AND_GROUPS)
    Promise<UserInformation> getUserInformation(String userName);

    @RequirePermission(ConfluencePermissions.MODIFY_USERS)
    Promise<Void> setUserPreferenceBoolean(String userName, String key, boolean value);

    @RequirePermission(ConfluencePermissions.READ_USERS_AND_GROUPS)
    Promise<Boolean> getUserPreferenceBoolean(String userName, String key);

    @RequirePermission(ConfluencePermissions.MODIFY_USERS)
    Promise<Void> setUserPreferenceLong(String userName, String key, long value);

    @RequirePermission(ConfluencePermissions.READ_USERS_AND_GROUPS)
    Promise<Long> getUserPreferenceLong(String userName, String key);

    @RequirePermission(ConfluencePermissions.MODIFY_USERS)
    Promise<Void> setUserPreferenceString(String userName, String key, String value);

    @RequirePermission(ConfluencePermissions.READ_USERS_AND_GROUPS)
    Promise<String> getUserPreferenceString(String userName, String key);

    @RequirePermission(ConfluencePermissions.READ_USERS_AND_GROUPS)
    Promise<Boolean> hasUser(String userName);

    @RequirePermission(ConfluencePermissions.READ_USERS_AND_GROUPS)
    Promise<Boolean> hasGroup(String groupName);

    @RequirePermission(ConfluencePermissions.MODIFY_USERS)
    Promise<Void> addProfilePicture(String userName, String fileName, String mimeType, byte[] pictureData);
}
