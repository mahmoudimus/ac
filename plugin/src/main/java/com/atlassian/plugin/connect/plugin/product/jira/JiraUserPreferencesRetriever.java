package com.atlassian.plugin.connect.plugin.product.jira;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.UserPreferencesRetriever;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.TimeZone;

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
        ApplicationUser user = userManager.getUserByName(userName);
        JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(user);
        if (user != null)
        {
            return timeZoneService.getUserTimeZoneInfo(jiraServiceContext).toTimeZone();
        }
        else
        {
            return timeZoneService.getDefaultTimeZoneInfo(jiraServiceContext).toTimeZone();
        }
    }
}
