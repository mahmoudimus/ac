package com.atlassian.plugin.connect.jira.workflow;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextFilter;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

import java.util.Map;
import java.util.UUID;


public class RemoteWorkflowFunctionPluginFactory extends AbstractWorkflowPluginFactory
        implements WorkflowPluginFunctionFactory
{
    // I hate these strings - but we can't change them without breaking backwards compatibility
    public static final String STORED_POSTFUNCTION_ID = "remoteWorkflowPostFunctionUUID";
    public static final String STORED_POSTFUNCTION_CONFIG = "remoteWorkflowPostFunctionConfiguration";

    @Override
    protected void getVelocityParamsForInput(final Map<String, Object> velocityParams)
    {
        velocityParams.put(JiraModuleContextFilter.POSTFUNCTION_ID, UUID.randomUUID().toString());
    }

    @Override
    protected void getVelocityParamsForView(final Map<String, Object> velocityParams, final AbstractDescriptor descriptor)
    {
        final FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
        velocityParams.put(JiraModuleContextFilter.POSTFUNCTION_ID, functionDescriptor.getArgs().get(STORED_POSTFUNCTION_ID));
        velocityParams.put(JiraModuleContextFilter.POSTFUNCTION_CONFIG, functionDescriptor.getArgs().get(STORED_POSTFUNCTION_CONFIG));
    }

    @Override
    protected void getVelocityParamsForEdit(final Map<String, Object> velocityParams, final AbstractDescriptor descriptor)
    {
        getVelocityParamsForView(velocityParams, descriptor);
    }

    @Override
    public Map<String, ?> getDescriptorParams(final Map<String, Object> formParams)
    {
        final String uuid = extractSingleParam(formParams, JiraModuleContextFilter.POSTFUNCTION_ID);
        final String functionConfiguration = extractSingleParam(formParams, JiraModuleContextFilter.POSTFUNCTION_CONFIG + "-" + uuid);

        return ImmutableMap.of(
                STORED_POSTFUNCTION_CONFIG, functionConfiguration,
                STORED_POSTFUNCTION_ID, uuid
        );
    }

}

