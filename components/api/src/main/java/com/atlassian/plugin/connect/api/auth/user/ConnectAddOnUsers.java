package com.atlassian.plugin.connect.api.auth.user;

import com.atlassian.crowd.model.user.User;

/**
 * Finds connect add-on users
 */
public interface ConnectAddOnUsers
{
    Iterable<User> getAddonUsers();
}
