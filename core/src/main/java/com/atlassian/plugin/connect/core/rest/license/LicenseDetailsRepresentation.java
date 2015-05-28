package com.atlassian.plugin.connect.core.rest.license;

import java.util.Date;

import com.atlassian.plugin.connect.api.service.license.RemotablePluginLicense;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * fixme: this is copied from UPM master at 75cee855ebd6475a3e7d9b619694e613c8906f09
 *
 * Remove this once UPM supports this rest resource
 */
public final class LicenseDetailsRepresentation implements RemotablePluginLicense
{
    @JsonProperty
    private final boolean valid;
    @JsonProperty
    private final boolean evaluation;
    @JsonProperty
    private final boolean nearlyExpired;
    @JsonProperty
    private final Integer maximumNumberOfUsers;
    @JsonProperty
    private final Date maintenanceExpiryDate;
    @JsonProperty
    private final String licenseType;
    @JsonProperty
    private final String creationDateString;
    @JsonProperty
    private final Date expiryDate;
    @JsonProperty
    private final String maintenanceExpiryDateString;
    @JsonProperty
    private final String supportEntitlementNumber;
    @JsonProperty
    private final String organizationName;
    @JsonProperty
    private final String contactEmail;
    @JsonProperty
    private final boolean enterprise;

    @JsonCreator
    public LicenseDetailsRepresentation(@JsonProperty("valid") Boolean valid,
                                        @JsonProperty("evaluation") Boolean evaluation,
                                        @JsonProperty("nearlyExpired") Boolean nearlyExpired,
                                        @JsonProperty("maximumNumberOfUsers") Integer maximumNumberOfUsers,
                                        @JsonProperty("maintenanceExpiryDate") Date maintenanceExpiryDate,
                                        @JsonProperty("licenseType") String licenseType,
                                        @JsonProperty("creationDateString") String creationDateString,
                                        @JsonProperty("expiryDate") Date expiryDate,
                                        @JsonProperty("maintenanceExpiryDateString") String maintenanceExpiryDateString,
                                        @JsonProperty("pluginSupportEntitlementNumber") String supportEntitlementNumber,
                                        @JsonProperty("organizationName") String organizationName,
                                        @JsonProperty("contactEmail") String contactEmail,
                                        @JsonProperty("enterprise") Boolean enterprise)
    {
        this.valid = (valid == null) ? false : valid.booleanValue();
        this.evaluation = (evaluation == null) ? false : evaluation.booleanValue();
        this.nearlyExpired = (nearlyExpired == null) ? false : nearlyExpired.booleanValue();
        this.maximumNumberOfUsers = maximumNumberOfUsers;
        this.licenseType = licenseType;
        this.maintenanceExpiryDate = maintenanceExpiryDate;
        this.maintenanceExpiryDateString = maintenanceExpiryDateString;
        this.creationDateString = creationDateString;
        this.expiryDate = expiryDate;
        this.supportEntitlementNumber = supportEntitlementNumber;
        this.organizationName = organizationName;
        this.contactEmail = contactEmail;
        this.enterprise = (enterprise == null) ? false : enterprise.booleanValue();
    }

    @Override
    public boolean isValid()
    {
        return valid;
    }

    @Override
    public boolean isEvaluation()
    {
        return evaluation;
    }

    @Override
    public boolean isNearlyExpired()
    {
        return nearlyExpired;
    }

    @Override
    public Integer getMaximumNumberOfUsers()
    {
        return maximumNumberOfUsers;
    }

    @Override
    public Date getMaintenanceExpiryDate()
    {
        return maintenanceExpiryDate;
    }

    @Override
    public String getMaintenanceExpiryDateString()
    {
        return maintenanceExpiryDateString;
    }

    @Override
    public String getLicenseType()
    {
        return licenseType;
    }

    @Override
    public String getCreationDateString()
    {
        return creationDateString;
    }
    
    @Override
    public Date getExpiryDate()
    {
        return expiryDate;
    }

    @Override
    public String getSupportEntitlementNumber()
    {
        return supportEntitlementNumber;
    }

    @Override
    public String getOrganizationName()
    {
        return organizationName;
    }

    @Override
    public String getContactEmail()
    {
        return contactEmail;
    }

    @Override
    public boolean isEnterprise()
    {
        return enterprise;
    }
}