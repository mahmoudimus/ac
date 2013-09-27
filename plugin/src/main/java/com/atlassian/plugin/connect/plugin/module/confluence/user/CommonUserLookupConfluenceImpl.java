package com.atlassian.plugin.connect.plugin.module.confluence.user;

import com.atlassian.plugin.connect.plugin.module.common.user.CommonUserLookup;
import com.atlassian.user.EntityException;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUserLookupConfluenceImpl implements CommonUserLookup<User>
{
    private final UserManager userManager;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public CommonUserLookupConfluenceImpl(UserManager userManager)
    {
        this.userManager = userManager;
    }

    @Override
    public User lookupByUsername(String username)
    {
        try
        {
            return userManager.getUser(username);
        }
        catch (EntityException e)
        {
            logger.warn("Unexpected exception when looking up a Confluence user. Continuing as anonymous");
            return null;
        }
    }
}
