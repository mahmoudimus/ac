package com.atlassian.plugin.connect.plugin.module.jira.conditions;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Verifies if a user has permissions to edit the configuration of a project.
 */
public class IsProjectAdminCondition implements Condition {

	private Project project;
	private final JiraAuthenticationContext authenticationContext;

	public IsProjectAdminCondition(JiraAuthenticationContext authenticationContext)
	{
		this.authenticationContext = authenticationContext;
	}

	@Override
	public void init(Map<String, String> params) throws PluginParseException
	{
	}

	@Override
	public boolean shouldDisplay(Map<String, Object> ctx)
	{
        boolean display = false;

        if (null != this.project && null != this.project.getKey())
        {
            final ProjectService projectService = ComponentAccessor.getComponent(ProjectService.class);
            ProjectService.GetProjectResult getProjectResult = projectService.getProjectByKeyForAction(authenticationContext.getUser(),
                    this.project.getKey(), ProjectAction.EDIT_PROJECT_CONFIG);
            display = null != getProjectResult && getProjectResult.isValid();
        }

        return display;
	}

	public void setProject(Project project)
	{
		this.project = project;
	}
}
