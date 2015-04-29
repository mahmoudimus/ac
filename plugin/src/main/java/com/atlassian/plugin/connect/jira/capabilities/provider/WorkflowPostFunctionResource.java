package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;

public enum WorkflowPostFunctionResource
{
    CREATE(JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS),
    VIEW(JiraWorkflowPluginConstants.RESOURCE_NAME_VIEW),
    EDIT(JiraWorkflowPluginConstants.RESOURCE_NAME_EDIT_PARAMETERS);

    private final String resource;

    WorkflowPostFunctionResource(final String resource)
    {
        this.resource = resource;
    }

    public String getResource()
    {
        return resource;
    }
}
