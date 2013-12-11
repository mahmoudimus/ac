package com.atlassian.plugin.connect.plugin.product.confluence;

import java.util.TimeZone;

import javax.annotation.Nullable;

import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.UserPreferencesRetriever;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ConfluenceUserPreferencesRetriever implements UserPreferencesRetriever
{
    private final UserAccessor userAccessor;

    @Autowired
    public ConfluenceUserPreferencesRetriever(final UserAccessor userAccessor)
    {
        this.userAccessor = userAccessor;
    }

    @Override
    public TimeZone getTimeZoneFor(@Nullable String userName)
    {
        ConfluenceUser user = userAccessor.getUserByName(userName);
        return userAccessor.getConfluenceUserPreferences(user).getTimeZone().getWrappedTimeZone();
    }
}
