package com.atlassian.plugin.connect.confluence;

import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.connect.plugin.UserPreferencesRetriever;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.TimeZone;

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
