package com.atlassian.plugin.connect.confluence;

import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.connect.spi.module.CurrentUserProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ConfluenceCurrentUserProvider implements CurrentUserProvider<ConfluenceUser>
{

    private UserManager userManager;
    private UserAccessor userAccessor;

    @Autowired
    public ConfluenceCurrentUserProvider(UserManager userManager, UserAccessor userAccessor)
    {
        this.userManager = userManager;
        this.userAccessor = userAccessor;
    }

    @Override
    public Class<ConfluenceUser> getUserType()
    {
        return ConfluenceUser.class;
    }

    @Override
    public ConfluenceUser getCurrentUser()
    {
        UserKey userKey = userManager.getRemoteUserKey();
        return userKey == null ? null : userAccessor.getExistingUserByKey(userKey);
    }
}
