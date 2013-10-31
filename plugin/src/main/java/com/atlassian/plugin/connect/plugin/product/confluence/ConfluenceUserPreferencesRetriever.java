package com.atlassian.plugin.connect.plugin.product.confluence;

import java.util.TimeZone;

import javax.annotation.Nullable;

import com.atlassian.plugin.connect.plugin.UserPreferencesRetriever;
import com.atlassian.plugin.connect.plugin.capabilities.annotation.ProductFilter;
import com.atlassian.plugin.connect.plugin.spring.ScopedComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ScopedComponent(products = {ProductFilter.CONFLUENCE})
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
