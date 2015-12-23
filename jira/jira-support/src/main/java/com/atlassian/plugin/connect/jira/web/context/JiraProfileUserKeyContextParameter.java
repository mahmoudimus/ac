package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@JiraComponent
public class JiraProfileUserKeyContextParameter implements JiraProfileUserContextParameterMapper.UserParameter
{

    private static final String PARAMETER_KEY = "profileUser.key";

    private JiraAuthenticationContext authenticationContext;

    @Autowired
    public JiraProfileUserKeyContextParameter(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public boolean isAccessibleByCurrentUser(ApplicationUser contextValue)
    {
        return isProfileUserAccessibleByCurrentUser();
    }

    @Override
    public boolean isValueAccessibleByCurrentUser(String value)
    {
        return isProfileUserAccessibleByCurrentUser();
    }

    private boolean isProfileUserAccessibleByCurrentUser()
    {
        return authenticationContext.isLoggedInUser();
    }

    @Override
    public String getKey()
    {
        return PARAMETER_KEY;
    }

    @Override
    public String getValue(ApplicationUser contextValue)
    {
        return contextValue.getKey();
    }
}
