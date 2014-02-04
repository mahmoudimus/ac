package com.atlassian.plugin.connect.plugin.module.jira.conditions;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import static com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextFilter.PROJECT_KEY;

/**
 * Verifies if a user has permissions to edit the configuration of a project.
 */
public class IsProjectAdminCondition implements Condition
{
    private static final String PROJECT_REQ_ATTR = "com.atlassian.jira.projectconfig.util.ServletRequestProjectConfigRequestCache:project";

    private final JiraAuthenticationContext authenticationContext;
    private final ProjectService projectService;

    public IsProjectAdminCondition(JiraAuthenticationContext authenticationContext, ProjectService projectService)
    {
        this.authenticationContext = authenticationContext;
        this.projectService = projectService;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> ctx)
    {
        ProjectService.GetProjectResult getProjectResult = projectService.getProjectByKeyForAction(
                authenticationContext.getUser(),
                getProject().getKey(),
                ProjectAction.EDIT_PROJECT_CONFIG
        );
        return getProjectResult != null && getProjectResult.isValid();
    }

    /**
     * Workaround JRA-26407 by attempting to resolve the project from a request attribute or the project.key query
     * parameter.
     * @return the context project
     */
    private Project getProject()
    {
        HttpServletRequest req = ExecutingHttpRequest.get();
        if (req == null)
        {
            throw new IllegalStateException("No " + HttpServletRequest.class.getSimpleName() + " context, can't resolve project!");
        }
        Project project = (Project) req.getAttribute(PROJECT_REQ_ATTR);
        if (project == null)
        {
            Object projectKey = req.getParameterMap().get(PROJECT_KEY);
            if (!(projectKey instanceof String[]))
            {
                throw new IllegalStateException("No " + PROJECT_KEY + " parameter found in the query string!");
            }
            final String key = ((String[]) projectKey)[0];
            project = ComponentManager.getComponent(ProjectManager.class).getProjectObjByKey(key);
            if (project == null)
            {
                throw new IllegalStateException("No project with key " + key + "!");
            }
            req.setAttribute(PROJECT_REQ_ATTR, project);
        }
        return project;
    }
}
