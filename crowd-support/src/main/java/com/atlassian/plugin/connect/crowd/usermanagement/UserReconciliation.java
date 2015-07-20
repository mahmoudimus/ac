package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.model.user.UserTemplate;

import com.google.common.base.Optional;

/**
 * A utility intented to be used within the crowd-support package only,
 * for determining whether any fixes need to be made to an add-on user
 */
public interface UserReconciliation
{
    /**
     * Check whether the user has the name, address and active status we expect.
     *
     * @param user the user to check
     * @param requiredDisplayName the expected display name
     * @param requiredEmailAddress the expected email address
     * @param active the expected active status
     * @return A {@link UserTemplate} containing the required fixes if any, otherwise <tt>none()</tt>
     */
    Optional<UserTemplate> getFixes(User user, String requiredDisplayName, String requiredEmailAddress, boolean active);
}
