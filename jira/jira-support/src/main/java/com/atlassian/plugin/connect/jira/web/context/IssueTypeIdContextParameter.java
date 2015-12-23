package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.config.IssueTypeService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@JiraComponent
public class IssueTypeIdContextParameter implements IssueContextParameterMapper.IssueParameter
{

    private static final String PARAMETER_KEY = "issuetype.id";

    private JiraAuthenticationContext authenticationContext;
    private IssueTypeService issueTypeService;

    @Autowired
    public IssueTypeIdContextParameter(JiraAuthenticationContext authenticationContext,
            IssueTypeService issueTypeService)
    {
        this.authenticationContext = authenticationContext;
        this.issueTypeService = issueTypeService;
    }

    @Override
    public boolean isAccessibleByCurrentUser(Issue contextValue)
    {
        return isIssueTypeAccessibleByCurrentUser(contextValue.getIssueTypeId());
    }

    @Override
    public boolean isValueAccessibleByCurrentUser(String value)
    {
        return isIssueTypeAccessibleByCurrentUser(value);
    }

    private boolean isIssueTypeAccessibleByCurrentUser(String issueTypeId)
    {
        ApplicationUser loggedInUser = authenticationContext.getLoggedInUser();
        return issueTypeService.getIssueType(loggedInUser, issueTypeId).isDefined();
    }

    @Override
    public String getKey()
    {
        return PARAMETER_KEY;
    }

    @Override
    public String getValue(Issue contextValue)
    {
        return contextValue.getIssueTypeId();
    }
}
