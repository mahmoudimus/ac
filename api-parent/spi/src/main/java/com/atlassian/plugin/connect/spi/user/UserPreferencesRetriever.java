package com.atlassian.plugin.connect.spi.user;

import java.util.TimeZone;

import javax.annotation.Nullable;

/**
 * Allows to get user preferences (for example the time zone). Some preferences will return default (for the
 * application) values when no user is specified.
 *
 * @since 0.6
 */
public interface UserPreferencesRetriever
{
    /**
     * Returns timezone for specified user. If specified user is null or doesn't exists, then the default time zone for
     * application is returned.
     *
     * @param userName A name of the user. Null can be passed to get the default time zone.
     * @return time zone for specified user or the default time zone.
     */
    TimeZone getTimeZoneFor(@Nullable String userName);
}
