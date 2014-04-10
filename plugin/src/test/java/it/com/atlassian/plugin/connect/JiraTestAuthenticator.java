package it.com.atlassian.plugin.connect;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;


public class JiraTestAuthenticator implements TestAuthenticator
{
    private final UserManager userManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public JiraTestAuthenticator(UserManager userManager, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.userManager = userManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public void authenticateUser(String username)
    {
        ApplicationUser user = userManager.getUserByName("admin");
        jiraAuthenticationContext.setLoggedInUser(user);
    }

    @Override
    public void unauthenticate()
    {
        jiraAuthenticationContext.clearLoggedInUser();
    }
}
