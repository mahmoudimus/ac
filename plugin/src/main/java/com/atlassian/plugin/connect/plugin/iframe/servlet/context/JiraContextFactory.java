package com.atlassian.plugin.connect.plugin.iframe.servlet.context;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;

import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_HELPER;
import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_USER;

/**
 * This context factory return context with JIRA's jira helper and user. These params are required to check whether user
 * has permission to view issue or project.
 *
 * @since v1.1.20
 */

@JiraComponent
public class JiraContextFactory implements ProductSpecificContextFactory
{

    private final JiraAuthenticationContext authenticationContext;
    private final IssueManager issueManager;
    private final ProjectService projectService;

    @Autowired
    public JiraContextFactory(JiraAuthenticationContext authenticationContext, IssueManager issueManager, ProjectService projectService)
    {
        this.authenticationContext = authenticationContext;
        this.issueManager = issueManager;
        this.projectService = projectService;
    }

    public Map<String, Object> createProductSpecificContext(final ModuleContextParameters params)
    {
        final ApplicationUser user = authenticationContext.getUser();
        if (user == null)
        {
            return Collections.emptyMap();
        }

        final JiraHelper helper = createJiraHelper(user, params);
        return ImmutableMap.of(CONTEXT_KEY_HELPER, helper, CONTEXT_KEY_USER, user);
    }

    private JiraHelper createJiraHelper(final ApplicationUser user, final Map<String, String> params)
    {
        final Issue issue = issueManager.getIssueObject(params.get(JiraModuleContextFilter.ISSUE_KEY));
        final Project project = projectService.getProjectByKey(user, params.get(JiraModuleContextFilter.PROJECT_KEY)).getProject();
        final Map<String, Object> helperParams = issue == null ? Collections.<String, Object>emptyMap() : ImmutableMap.<String, Object>of("issue", issue);

        return new JiraHelper(ExecutingHttpRequest.get(), project, helperParams);
    }
}
