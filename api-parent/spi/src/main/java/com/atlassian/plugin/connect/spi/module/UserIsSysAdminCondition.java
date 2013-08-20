package com.atlassian.plugin.connect.spi.module;

import java.util.Map;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Displays if the user is an admin as determined by SAL's isSystemAdmin()
 */
public final class UserIsSysAdminCondition implements Condition
{
    private final UserManager userManager;

    public UserIsSysAdminCondition(UserManager userManager)
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
        return userManager.isSystemAdmin(userManager.getRemoteUsername());
    }
}
