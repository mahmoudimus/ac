package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.connect.plugin.capabilities.annotation.ProductFilter;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.plugin.connect.plugin.spring.ScopedComponent;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes ProfileUser objects.
 */
@ScopedComponent(products = {ProductFilter.JIRA})
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
