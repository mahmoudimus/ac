package com.atlassian.plugin.connect.spi.condition;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Displays if the user is an admin as determined by SAL's isSystemAdmin()
 */
@Named
public final class UserIsSysAdminCondition implements Condition
{
    private final UserManager userManager;

    @Inject
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
        return userManager.isSystemAdmin(userManager.getRemoteUserKey());
    }
}
