package com.atlassian.plugin.connect.plugin.web.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.spi.web.context.ProductContextProducer;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class JiraProductContextProducer implements ProductContextProducer
{
    private final IssueManager issueManager;
    private final ProjectManager projectManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    @Autowired
    public JiraProductContextProducer(final IssueManager issueManager, final ProjectManager projectManager, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.issueManager = issueManager;
        this.projectManager = projectManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public Map<String, Object> produce(HttpServletRequest request, final Map<String, String> queryParams)
    {
        Optional<Issue> issue = mapParam(queryParams, "issue.id", id -> issueManager.getIssueObject(Long.valueOf(id)));
        Optional<Project> project = mapParam(queryParams, "project.id", id -> projectManager.getProjectObj(Long.valueOf(id)));

        Map<String, Object> context = new HashMap<>();
        context.put("user", jiraAuthenticationContext.getLoggedInUser());
        issue.ifPresent(value -> context.put("issue", value));
        project.ifPresent(value -> context.put("project", value));

        JiraHelper jiraHelper = new JiraHelper(request, project.orElse(null), context);

        context.put("helper", jiraHelper);

        return context;
    }

    private <T> Optional<T> mapParam(Map<String, String> queryParams, String paramName, Function<String, T> valueMapping)
    {
        return Optional.ofNullable(queryParams.get(paramName))
                .flatMap(valueMapping.andThen(Optional::ofNullable));
    }
}
