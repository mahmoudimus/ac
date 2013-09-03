package com.atlassian.connect.capabilities.client;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @since version
 */
public interface RemoteApplicationWithCapabilities
{
    
    /**
     * a canonical value representing the application type
     */
    @Nonnull
    String getType();

    /**
     * Build date of the application, or a 'null' date (01/01/1970 UTC) if unknown.
     *
     * @return build date of the application
     */
    @Nonnull
    DateTime getBuildDate();

    String getSelfUrl();

    /**
     *
     * @return true if the application exposes any capabilities
     */
    boolean hasCapabilities();

    /**
     *
     * @param key
     * @return true if the application has this capability
     */
    boolean hasCapability(String key);

    /**
     *
     * @param key
     * @return the url for the capability, or null if the application does not have this capability
     */
    @Nullable
    String getCapabilityUrl(String key);

    /**
     * the map of capabilities to capability urls provided by the application.
     * @return
     */
    @Nonnull
    Map<String, String> getCapabilities();
}
