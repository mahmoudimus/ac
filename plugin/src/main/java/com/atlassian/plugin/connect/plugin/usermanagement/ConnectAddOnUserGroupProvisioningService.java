package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.group.Group;

/**
 * Perform operations on user groups.
 */
public interface ConnectAddOnUserGroupProvisioningService
{
    /**
     * Ensure that the nominated user is in the nominated group. Add membership if the user is not already a member.
     * @param userKey the unique user identifier
     * @param groupKey the unique group identifier
     */
    void ensureUserIsInGroup(String userKey, String groupKey) throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException;

    /**
     * Remove the user from the nominated group. It is not an error condition if the user is not a member of the group.
     * @param userKey the unique user identifier
     * @param groupKey the unique group identifier
     */
    void removeUserFromGroup(String userKey, String groupKey) throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException;

    /**
     * Ensure that the nominated group exists. Create it if it doesn't already exist.
     * @param groupKey the unique group identifier
     * @return {@code true} if the group was created, otherwise {@code false}
     */
    boolean ensureGroupExists(String groupKey) throws ApplicationNotFoundException, OperationFailedException, ApplicationPermissionException;

    /**
     * Find a group by its unique identifier
     * @param groupKey the unique group identifier
     * @return the {@link Group} if it exists, otherwise {@code null}
     * @throws ApplicationNotFoundException if the current application does not exist in Crowd
     */
    public Group findGroupByKey(String groupKey) throws ApplicationNotFoundException;

    /**
     * We need to know the name of the {@link com.atlassian.crowd.model.application.Application} in Crowd so that we can find it.
     * @return the {@link String} unique name of the {@link com.atlassian.crowd.model.application.Application} in which we perform user management
     */
    String getCrowdApplicationName();
}
