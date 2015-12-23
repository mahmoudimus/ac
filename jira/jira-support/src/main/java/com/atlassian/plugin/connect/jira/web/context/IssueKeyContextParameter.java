package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@JiraComponent
public class IssueKeyContextParameter extends AbstractBrowsableIssueParameter
{

    private static final String PARAMETER_KEY = "issue.key";
    private final IssueManager issueManager;

    @Autowired
    public IssueKeyContextParameter(JiraAuthenticationContext authenticationContext,
            PermissionManager permissionManager, IssueManager issueManager)
    {
        super(authenticationContext, permissionManager);
        this.issueManager = issueManager;
    }

    @Override
    public boolean isValueAccessibleByCurrentUser(String value)
    {
        return Optional.ofNullable(issueManager.getIssueObject(value))
                .map(this::isAccessibleByCurrentUser)
                .orElse(false);
    }

    @Override
    public String getKey()
    {
        return PARAMETER_KEY;
    }

    @Override
    public String getValue(Issue contextValue)
    {
        return contextValue.getKey();
    }

}
