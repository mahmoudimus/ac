package com.atlassian.labs.remoteapps.spi.module;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Displays if the user is an admin as determined by SAL's isSystemAdmin()
 */
@Component
public class UserIsSysAdminCondition implements Condition
{
    private final UserManager userManager;

    @Autowired
    public UserIsSysAdminCondition(UserManager userManager)
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
        return userManager.isSystemAdmin(userManager.getRemoteUsername());
    }
}
