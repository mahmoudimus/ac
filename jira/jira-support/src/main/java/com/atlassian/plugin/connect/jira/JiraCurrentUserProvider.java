package com.atlassian.plugin.connect.jira;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.connect.spi.module.CurrentUserProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class JiraCurrentUserProvider implements CurrentUserProvider<ApplicationUser>
{

    private JiraAuthenticationContext authenticationContext;

    @Autowired
    public JiraCurrentUserProvider(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public Class<ApplicationUser> getUserType()
    {
        return ApplicationUser.class;
    }

    @Override
    public ApplicationUser getCurrentUser()
    {
        return authenticationContext.getLoggedInUser();
    }
}
