package it.com.atlassian.plugin.connect.testlifecycle;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;


/**
 * we have our own version of this because we can't use the test support installer due to it's dependency on connect modules
 */
public class JiraLifecycleTestAuthenticator implements LifecycleTestAuthenticator
{
    private final UserManager userManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public JiraLifecycleTestAuthenticator(UserManager userManager, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.userManager = userManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public void authenticateUser(String username)
    {
        User user = userManager.getUserObject("admin");
        jiraAuthenticationContext.setLoggedInUser(user);
    }
}