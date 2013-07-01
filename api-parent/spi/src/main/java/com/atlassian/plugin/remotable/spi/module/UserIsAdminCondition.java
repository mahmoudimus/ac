package com.atlassian.plugin.remotable.spi.module;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Displays if the user is an admin as determined by SAL's isAdmin()
 */
public final class UserIsAdminCondition implements Condition
{
    private final UserManager userManager;

    public UserIsAdminCondition(UserManager userManager)
    {
        this.userManager = checkNotNull(userManager);
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
