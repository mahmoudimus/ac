package com.atlassian.plugin.remotable.plugin.product.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.timezone.TimeZoneResolver;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.remotable.plugin.UserPreferencesRetriever;

import javax.annotation.Nullable;
import java.util.TimeZone;

public class JiraUserPreferencesRetriever implements UserPreferencesRetriever
{

    private final UserManager userManager;
    private final TimeZoneResolver timeZoneResolver;

    public JiraUserPreferencesRetriever(final UserManager userManager, final TimeZoneResolver timeZoneResolver)
    {
        this.userManager = userManager;
        this.timeZoneResolver = timeZoneResolver;
    }

    @Override
    public TimeZone getTimeZoneFor(@Nullable String userName)
    {
        final User user = userManager.getUser(userName);
        final JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(user);
        return (user == null) ? timeZoneResolver.getDefaultTimeZone(jiraServiceContext) : timeZoneResolver.getUserTimeZone(jiraServiceContext);
    }
}
