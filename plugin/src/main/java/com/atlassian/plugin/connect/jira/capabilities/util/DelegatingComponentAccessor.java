package com.atlassian.plugin.connect.jira.capabilities.util;

import com.atlassian.jira.component.ComponentAccessor;

import org.springframework.stereotype.Component;

@Component
public class DelegatingComponentAccessor
{
    public <T> T getComponent(Class<T> componentClass)
    {
        return ComponentAccessor.getComponent(componentClass);
    }
}
