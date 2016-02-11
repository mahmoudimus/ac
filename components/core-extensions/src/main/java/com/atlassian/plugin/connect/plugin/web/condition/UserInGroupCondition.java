package com.atlassian.plugin.connect.plugin.web.condition;

import java.util.Map;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import com.google.common.base.Strings;

import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public class UserInGroupCondition implements Condition
{
    private final UserManager userManager;

    private String groupName;

    public UserInGroupCondition(UserManager userManager)
    {
        this.userManager = checkNotNull(userManager);
    }

    @Override
    public void init(Map<String, String> map) throws PluginParseException
    {
        groupName = Strings.nullToEmpty(map.get("groupName"));

        if(StringUtils.isBlank(groupName)) {
            throw new PluginParseException("You need to provide a 'groupName' that is not blank to the user_in_group condition");
        }
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> map)
    {
        final UserProfile remoteUser = userManager.getRemoteUser();
        // Anonymous users are never in the group
        return remoteUser != null && userManager.isUserInGroup(remoteUser.getUserKey(), groupName);
    }
}
