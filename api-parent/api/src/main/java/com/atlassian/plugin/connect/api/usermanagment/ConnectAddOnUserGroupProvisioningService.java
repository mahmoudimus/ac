package com.atlassian.plugin.connect.api.usermanagment;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.group.Group;

/**
 * Perform operations on user groups.
 */
public interface ConnectAddOnUserGroupProvisioningService
{
    /**
     * Ensure that the nominated user is in the nominated group. Add membership if the user is not already a member.
     *
     * @param userKey the unique user identifier
     * @param groupKey the unique group identifier
     * @throws ApplicationNotFoundException if the Crowd application cannot be found
     * @throws UserNotFoundException when the user cannot be found in ANY directory
     * @throws GroupNotFoundException when the group cannot be found in ANY directory
     * @throws ApplicationPermissionException if the application's directory where the primary user resides does not allow operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP} or the group is readonly.
     * @throws InvalidAuthenticationException if the operation was carried out against a remote crowd instance and failed to authenticate.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void ensureUserIsInGroup(String userKey, String groupKey)
            throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, InvalidAuthenticationException;

    /**
     * Remove the user from the nominated group. It is not an error condition if the user is not a member of the group.
     *
     * @param userKey the unique user identifier
     * @param groupKey the unique group identifier
     * @throws ApplicationNotFoundException if the Crowd application cannot be found
     * @throws UserNotFoundException when the user cannot be found in ANY directory
     * @throws GroupNotFoundException when the group cannot be found in ANY directory
     * @throws ApplicationPermissionException if the application's directory where the primary user resides does not allow operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP} or the group is readonly.
     * @throws InvalidAuthenticationException if the operation was carried out against a remote crowd instance and failed to authenticate.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void removeUserFromGroup(String userKey, String groupKey)
            throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, InvalidAuthenticationException;

    /**
     * Ensure that the nominated group exists. Create it if it doesn't already exist.
     *
     * @param groupKey the unique group identifier
     * @return {@code true} if the group was created, otherwise {@code false}
     * @throws ApplicationNotFoundException if the Crowd application cannot be found
     * @throws ApplicationPermissionException if the application's directory where the primary user resides does not allow operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP} or the group is readonly.
     * @throws InvalidAuthenticationException if the operation was carried out against a remote crowd instance and failed to authenticate.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    boolean ensureGroupExists(String groupKey)
            throws ApplicationNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    /**
     * Find a group by its unique identifier.
     *
     * @param groupKey the unique group identifier
     * @return the {@link Group} if it exists, otherwise {@code null}
     * @throws ApplicationNotFoundException if the current application does not exist in Crowd
     */
    Group findGroupByKey(String groupKey)
            throws ApplicationNotFoundException, ApplicationPermissionException, InvalidAuthenticationException;
}
