package com.atlassian.plugin.remotable.plugin.product.refapp;

import com.atlassian.plugin.remotable.plugin.UserPreferencesRetriever;

import javax.annotation.Nullable;
import java.util.TimeZone;

public class RefappUserPreferencesRetriever implements UserPreferencesRetriever
{

    /**
     * @return always the default timezone
     */
    @Override
    public TimeZone getTimeZoneFor(@Nullable String userName)
    {
        return TimeZone.getDefault();
    }
}
