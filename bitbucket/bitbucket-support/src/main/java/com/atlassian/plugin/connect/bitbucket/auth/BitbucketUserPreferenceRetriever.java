package com.atlassian.plugin.connect.bitbucket.auth;

import java.util.TimeZone;

import javax.annotation.Nullable;

import com.atlassian.plugin.connect.spi.UserPreferencesRetriever;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;

@BitbucketComponent
public class BitbucketUserPreferenceRetriever implements UserPreferencesRetriever
{
    @Override
    public TimeZone getTimeZoneFor(@Nullable String userName)
    {
        return TimeZone.getDefault();
    }
}
