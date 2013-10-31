package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.plugin.connect.plugin.capabilities.annotation.ProductFilter;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.plugin.connect.plugin.spring.ScopedComponent;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes ProjectComponent objects.
 */
@ScopedComponent(products = {ProductFilter.JIRA})
public class ComponentSerializer implements ParameterSerializer<ProjectComponent>
{
    @Override
    public Map<String, Object> serialize(final ProjectComponent projectComponent)
    {
        return ImmutableMap.<String, Object>of("component",
                ImmutableMap.of("id", projectComponent.getId()));
    }
}
