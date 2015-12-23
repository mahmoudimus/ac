package com.atlassian.plugin.connect.jira.web.condition;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.jira.web.context.ProjectKeyContextParameter;
import com.atlassian.plugin.web.Condition;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Verifies if a user has permissions to edit the configuration of a project.
 */
public class IsProjectAdminCondition implements Condition
{
    private static final String PROJECT_REQ_ATTR = "com.atlassian.jira.projectconfig.util.ServletRequestProjectConfigRequestCache:project";

    private final JiraAuthenticationContext authenticationContext;
    private final ProjectService projectService;
    private ProjectKeyContextParameter projectKeyContextParameter;

    public IsProjectAdminCondition(JiraAuthenticationContext authenticationContext, ProjectService projectService,
            ProjectKeyContextParameter projectKeyContextParameter)
    {
        this.authenticationContext = authenticationContext;
        this.projectService = projectService;
        this.projectKeyContextParameter = projectKeyContextParameter;
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
                getProject(ctx).getKey(),
                ProjectAction.EDIT_PROJECT_CONFIG
        );
        return getProjectResult != null && getProjectResult.isValid();
    }

    /**
     * Resolve the project object from the context if present, otherwise workaround JRA-26407 by attempting to resolve
     * the project from a request attribute or the project.key query parameter.
     *
     * @return the context project
     */
    private Project getProject(final Map<String, Object> ctx)
    {
        HttpServletRequest req = ExecutingHttpRequest.get();
        Project project;

        // first try to resolve the project from the context
        Object projectObj = ctx.get("project");
        if (projectObj instanceof Project)
        {
            project = (Project) projectObj;
        }
        else
        {
            // otherwise check to see if it's been cached as a request attribute
            if (req == null)
            {
                throw new IllegalStateException("No " + HttpServletRequest.class.getSimpleName() +
                        " context, can't resolve project!");
            }
            project = (Project) req.getAttribute(PROJECT_REQ_ATTR);
            if (project == null)
            {
                // otherwise see if there's a request parameter specifying the project
                String projectContextParameterKey = projectKeyContextParameter.getKey();
                Object projectKey = req.getParameterMap().get(projectContextParameterKey);
                if (!(projectKey instanceof String[]))
                {
                    throw new IllegalStateException("No " + projectContextParameterKey + " parameter found in the query string!");
                }
                final String key = ((String[]) projectKey)[0];
                project = ComponentManager.getComponent(ProjectManager.class).getProjectObjByKey(key);
                if (project == null)
                {
                    throw new IllegalStateException("No project with key " + key + "!");
                }
            }
        }

        if (req != null)
        {
            // cache project as request attribute
            req.setAttribute(PROJECT_REQ_ATTR, project);
        }

        return project;
    }
}
