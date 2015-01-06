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
    private final boolean active;

    @JsonProperty
    private final LicenseType type;

    @JsonProperty
    private final boolean evaluation;

    @JsonProperty
    private final String supportEntitlementNumber;

    public RestAddonLicense(@JsonProperty("active") final boolean active,
                            @JsonProperty("type") final LicenseType type,
                            @JsonProperty("evaluation") final boolean evaluation,
                            @JsonProperty("supportEntitlementNumber") String supportEntitlementNumber)
    {
        this.active = active;
        this.type = type;
        this.evaluation = evaluation;
        this.supportEntitlementNumber = supportEntitlementNumber;
    }

    public boolean isActive()
    {
        return active;
    }

    public LicenseType getType()
    {
        return type;
    }

    public boolean isEvaluation()
    {
        return evaluation;
    }

    public String getSupportEntitlementNumber()
    {
        return supportEntitlementNumber;
    }
}
