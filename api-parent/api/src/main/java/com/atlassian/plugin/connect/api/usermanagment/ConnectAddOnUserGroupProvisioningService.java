package com.atlassian.plugin.connect.api.usermanagment;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.application.Application;
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
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void ensureUserIsInGroup(String userKey, String groupKey) throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException;

    /**
     * Remove the user from the nominated group. It is not an error condition if the user is not a member of the group.
     *
     * @param userKey the unique user identifier
     * @param groupKey the unique group identifier
     * @throws ApplicationNotFoundException if the Crowd application cannot be found
     * @throws UserNotFoundException when the user cannot be found in ANY directory
     * @throws GroupNotFoundException when the group cannot be found in ANY directory
     * @throws ApplicationPermissionException if the application's directory where the primary user resides does not allow operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP} or the group is readonly.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void removeUserFromGroup(String userKey, String groupKey) throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException;

    /**
     * Ensure that the nominated group exists. Create it if it doesn't already exist.
     *
     * @param groupKey the unique group identifier
     * @return {@code true} if the group was created, otherwise {@code false}
     * @throws ApplicationNotFoundException if the Crowd application cannot be found
     * @throws ApplicationPermissionException if the application's directory where the primary user resides does not allow operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP} or the group is readonly.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    boolean ensureGroupExists(String groupKey) throws ApplicationNotFoundException, OperationFailedException, ApplicationPermissionException;

    /**
     * Find a group by its unique identifier.
     *
     * @param groupKey the unique group identifier
     * @return the {@link Group} if it exists, otherwise {@code null}
     * @throws ApplicationNotFoundException if the current application does not exist in Crowd
     */
    public Group findGroupByKey(String groupKey) throws ApplicationNotFoundException;

    /**
     * We need to know the name of the {@link Application} in Crowd so that we can find it.
     * @return the {@link String} unique name of the {@link Application} in which we perform user management
     */
    String getCrowdApplicationName();

    /**
     * @return the {@link Application} in which we perform user management
     *
     * <strong>Do not cache</strong> the returned Application; it is immutable
     * and replaced every time a change is made
     *
     * @throws ApplicationNotFoundException application with requested name does not exist
     */
    Application getCrowdApplication() throws ApplicationNotFoundException;

}
