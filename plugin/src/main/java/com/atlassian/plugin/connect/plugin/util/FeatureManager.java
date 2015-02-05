package com.atlassian.plugin.connect.plugin.util;

/**
 * Responsible for providing information to Atlassian Connect to determine if certain product features are
 * enabled/disabled.
 */
public interface FeatureManager
{
    /**
     * Is the product running Atlassian Connect running in OnDemand mode
     * @return true if the product is running in OnDemand mode
     */
    boolean isOnDemand();
}
