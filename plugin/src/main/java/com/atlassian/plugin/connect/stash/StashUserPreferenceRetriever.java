package com.atlassian.plugin.connect.stash;

import java.util.TimeZone;

import javax.annotation.Nullable;

import com.atlassian.plugin.connect.spi.user.UserPreferencesRetriever;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;

@StashComponent
public class StashUserPreferenceRetriever implements UserPreferencesRetriever
{
    @Override
    public TimeZone getTimeZoneFor(@Nullable String userName)
    {
        return TimeZone.getDefault();
    }
}
