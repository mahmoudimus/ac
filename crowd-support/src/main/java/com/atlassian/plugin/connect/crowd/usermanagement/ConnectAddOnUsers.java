package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.model.user.User;

/**
 * Finds connect add-on users
 *
 * <strong>Not suitable for general-purpose add-on user management</strong>
 */
public interface ConnectAddOnUsers
{
    Iterable<User> getAddonUsers();
}
