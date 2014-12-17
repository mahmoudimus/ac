package com.atlassian.plugin.connect.plugin.rest.data;

import com.atlassian.plugin.connect.plugin.license.LicenseStatus;
import com.atlassian.upm.api.license.entity.LicenseType;
import org.codehaus.jackson.annotate.JsonProperty;

public class RestAddonLicense
{
    @JsonProperty
    private final LicenseStatus status;

    @JsonProperty
    private final LicenseType type;

    @JsonProperty
    private final boolean evaluation;

    public RestAddonLicense(@JsonProperty("status") final LicenseStatus status,
                            @JsonProperty("type") final LicenseType type,
                            @JsonProperty("evaluation") final boolean evaluation)
    {
        this.status = status;
        this.type = type;
        this.evaluation = evaluation;
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
}
