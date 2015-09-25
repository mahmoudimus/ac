package com.atlassian.plugin.connect.spi.product;

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

    /**
     * Are admin permissions managed via Horde Unified User Management
     * (indicated by the presence of the {@link com.atlassian.jira.config.CoreFeatures#PERMISSIONS_MANAGED_BY_UM} flag)
     *
     * @return true if admin permissions are managed via Horde Unified User Management
     * (meaning they must be set remotely, or else will be overwritten)
     */
    boolean isPermissionsManagedByUM();
}
