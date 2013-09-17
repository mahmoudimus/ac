package com.atlassian.plugin.connect.plugin.module.confluence.user;

import com.atlassian.plugin.connect.plugin.module.common.user.CommonUserLookup;
import com.atlassian.plugin.connect.plugin.module.common.user.UserLookupException;
import com.atlassian.user.EntityException;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;

public class CommonUserLookupConfluenceImpl implements CommonUserLookup<User>
{
    private final UserManager userManager;

    public CommonUserLookupConfluenceImpl(UserManager userManager)
    {
        this.userManager = userManager;
    }

    @Override
    public User lookupByUsername(String username) throws UserLookupException
    {
        try
        {
            return userManager.getUser(username);
        }
        catch (EntityException e)
        {
            throw new UserLookupException(e);
        }
    }
}
