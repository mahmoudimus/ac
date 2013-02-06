package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.plugin.UserPreferencesRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.TimeZone;

public class ConfluenceUserPreferencesRetriever implements UserPreferencesRetriever
{

    private static final Logger log = LoggerFactory.getLogger(ConfluenceUserPreferencesRetriever.class);

    @Override
    public TimeZone getTimeZoneFor(@Nullable String userName)
    {
        // TODO: implement, ARA-302
        log.warn(getClass() + "#getTimeZoneFor is not yet implemented!");
        return TimeZone.getDefault();
    }
}
