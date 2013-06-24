package com.atlassian.plugin.remotable.api.service.license;

import java.util.Date;

/**
 * License for a remotable plugin
 *
 * @since 0.6.8
 */
public interface RemotablePluginLicense
{
    boolean isValid();

    boolean isEvaluation();

    boolean isNearlyExpired();

    Integer getMaximumNumberOfUsers();

    Date getMaintenanceExpiryDate();

    String getMaintenanceExpiryDateString();

    String getLicenseType();

    String getCreationDateString();

    Date getExpiryDate();

    String getSupportEntitlementNumber();

    String getOrganizationName();

    String getContactEmail();

    boolean isEnterprise();
}
