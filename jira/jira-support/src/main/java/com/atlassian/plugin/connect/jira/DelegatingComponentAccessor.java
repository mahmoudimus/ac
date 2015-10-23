package com.atlassian.plugin.connect.jira;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

@JiraComponent
public class DelegatingComponentAccessor
{
    public <T> T getComponent(Class<T> componentClass)
    {
        return ComponentAccessor.getComponent(componentClass);
    }
}
