package com.atlassian.plugin.remotable.plugin.module.jira.workflow;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.loader.AbstractDescriptor;

import java.util.Map;


public class RemoteWorkflowFunctionPluginFactory extends AbstractWorkflowPluginFactory
        implements WorkflowPluginFunctionFactory
{
    @Override
    protected void getVelocityParamsForInput(final Map<String, Object> velocityParams)
    {
        // TODO implement during post-function configuration.
    }

    @Override
    protected void getVelocityParamsForEdit(final Map<String, Object> velocityParams, final AbstractDescriptor descriptor)
    {
        // TODO implement during post-function configuration.
    }

    @Override
    protected void getVelocityParamsForView(final Map<String, Object> velocityParams, final AbstractDescriptor descriptor)
    {
        // TODO implement during post-function configuration
    }

    @Override
    public Map<String, ?> getDescriptorParams(final Map<String, Object> formParams)
    {
        // TODO implement during post-function configuration.
        return ImmutableMap.of();
    }
}

