package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.connect.spi.module.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes ProfileUser objects.
 */
@JiraComponent
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
