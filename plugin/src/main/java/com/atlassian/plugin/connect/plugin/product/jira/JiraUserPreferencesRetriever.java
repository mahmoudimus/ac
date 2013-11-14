package com.atlassian.plugin.connect.plugin.product.jira;

import java.util.TimeZone;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.timezone.TimeZoneInfo;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.UserPreferencesRetriever;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class JiraUserPreferencesRetriever implements UserPreferencesRetriever
{

    private final UserManager userManager;
    private final TimeZoneService timeZoneService;

    @Autowired
    public JiraUserPreferencesRetriever(final UserManager userManager, final TimeZoneService timeZoneService)
    {
        this.userManager = userManager;
        this.timeZoneService = timeZoneService;
    }

    @Override
    public TimeZone getTimeZoneFor(@Nullable String userName)
    {
        final User user = userManager.getUser(userName);
        final JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(user);
        final TimeZoneInfo timeZoneInfo = (user != null) ? timeZoneService.getUserTimeZoneInfo(jiraServiceContext) : timeZoneService.getDefaultTimeZoneInfo(jiraServiceContext);
        return timeZoneInfo.toTimeZone();
    }
}
