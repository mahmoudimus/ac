package com.atlassian.plugin.remotable.test.refapp;

import com.atlassian.user.EntityException;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;

/**
 */
public class RefappFixEmailAddresses
{
    public RefappFixEmailAddresses(UserManager userManager) throws EntityException
    {
        User user = userManager.getUser("betty");
        user.setEmail("betty@example.com");
        userManager.saveUser(user);
    }
}
