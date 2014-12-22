package com.atlassian.plugin.connect.plugin.rest.data;

import com.atlassian.plugin.connect.plugin.license.LicenseStatus;
import com.atlassian.upm.api.license.entity.LicenseType;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A representation of an add-on license for the REST API.
 */
public class RestAddonLicense
{
    @JsonProperty
    private final LicenseStatus status;

    @JsonProperty
    private final LicenseType type;

    @JsonProperty
    private final boolean evaluation;
    private String contactEmail;
    private String supportEntitlementNumber;

    public RestAddonLicense(@JsonProperty("status") final LicenseStatus status,
                            @JsonProperty("type") final LicenseType type,
                            @JsonProperty("evaluation") final boolean evaluation,
                            @JsonProperty("contactEmail") String contactEmail,
                            @JsonProperty("supportEntitlementNumber") String supportEntitlementNumber)
    {
        this.status = status;
        this.type = type;
        this.evaluation = evaluation;
        this.contactEmail = contactEmail;
        this.supportEntitlementNumber = supportEntitlementNumber;
    }

    public LicenseStatus getStatus()
    {
        return this.status;
    }

    public LicenseType getType()
    {
        return this.type;
    }

    public boolean isEvaluation()
    {
        return this.evaluation;
    }

    public String getContactEmail()
    {
        return this.contactEmail;
    }

    public String getSupportEntitlementNumber()
    {
        return this.supportEntitlementNumber;
    }
}
