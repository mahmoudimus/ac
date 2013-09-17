package com.atlassian.plugin.connect.plugin.module.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.common.user.CommonUserLookup;
import com.atlassian.plugin.connect.plugin.module.common.user.UserLookupException;

public class CommonUserLookupJiraImpl implements CommonUserLookup<User>
{
    private final UserManager userManager;

    public CommonUserLookupJiraImpl(UserManager userManager)
    {
        this.userManager = userManager;
    }

    @Override
    public User lookupByUsername(String username) throws UserLookupException
    {
        final ApplicationUser appUser = userManager.getUserByName(username);

        return appUser == null ? null : appUser.getDirectoryUser();
    }
}
