package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes ProfileUser objects.
 */
public class ProfileUserSerializer implements ParameterSerializer<ApplicationUser>
{
    @Override
    public Map<String, Object> serialize(ApplicationUser applicationUser)
    {
        return ImmutableMap.<String, Object>of("profileUser", ImmutableMap.of(
                "name", applicationUser.getName(),
                "key", applicationUser.getKey()
        ));
    }
}
