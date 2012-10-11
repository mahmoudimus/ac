package com.atlassian.plugin.remotable.spi.module;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Displays if the user is an admin as determined by SAL's isAdmin()
 */
@Component
public class UserIsAdminCondition implements Condition
{
    private final UserManager userManager;

    @Autowired
    public UserIsAdminCondition(UserManager userManager)
    {
        this.userManager = userManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return userManager.isAdmin(userManager.getRemoteUsername());
    }
}
