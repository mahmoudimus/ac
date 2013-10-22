package com.atlassian.plugin.connect.plugin.module.jira.workflow;

import java.util.Map;
import java.util.UUID;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;


public class RemoteWorkflowFunctionPluginFactory extends AbstractWorkflowPluginFactory
        implements WorkflowPluginFunctionFactory
{
    public static final String POST_FUNCTION_CONFIGURATION = "remoteWorkflowPostFunctionConfiguration";
    public static final String POST_FUNCTION_CONFIGURATION_UUID = "remoteWorkflowPostFunctionUUID";

    @Override
    protected void getVelocityParamsForInput(final Map<String, Object> velocityParams)
    {
        velocityParams.put(POST_FUNCTION_CONFIGURATION_UUID, UUID.randomUUID().toString());
    }

    @Override
    protected void getVelocityParamsForEdit(final Map<String, Object> velocityParams, final AbstractDescriptor descriptor)
    {
        final FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
        velocityParams.put(POST_FUNCTION_CONFIGURATION_UUID, functionDescriptor.getArgs().get(POST_FUNCTION_CONFIGURATION_UUID));
        velocityParams.put(POST_FUNCTION_CONFIGURATION, functionDescriptor.getArgs().get(POST_FUNCTION_CONFIGURATION));
    }

    @Override
    protected void getVelocityParamsForView(final Map<String, Object> velocityParams, final AbstractDescriptor descriptor)
    {
        final FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
        final String functionConfiguration = (String) functionDescriptor.getArgs().get(POST_FUNCTION_CONFIGURATION);
        final String uuid = (String) functionDescriptor.getArgs().get(POST_FUNCTION_CONFIGURATION_UUID);
        velocityParams.put(POST_FUNCTION_CONFIGURATION, functionConfiguration);
        velocityParams.put(POST_FUNCTION_CONFIGURATION_UUID, uuid);
    }

    @Override
    public Map<String, ?> getDescriptorParams(final Map<String, Object> formParams)
    {
        final String uuid = extractSingleParam(formParams, POST_FUNCTION_CONFIGURATION_UUID);
        final String functionConfiguration = extractSingleParam(formParams, POST_FUNCTION_CONFIGURATION+"-"+uuid);
        return ImmutableMap.of(POST_FUNCTION_CONFIGURATION, functionConfiguration,
                POST_FUNCTION_CONFIGURATION_UUID, uuid);
    }

}

