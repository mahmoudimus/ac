package com.atlassian.plugin.connect.plugin.module.jira.conditions;

import java.util.Map;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

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
		final ProjectService projectService = ComponentAccessor.getComponent(ProjectService.class);
		return projectService.getProjectByKeyForAction(authenticationContext.getUser(),
				project.getKey(), ProjectAction.EDIT_PROJECT_CONFIG).isValid();
	}

	public void setProject(Project project)
	{
		this.project = project;
	}
}
