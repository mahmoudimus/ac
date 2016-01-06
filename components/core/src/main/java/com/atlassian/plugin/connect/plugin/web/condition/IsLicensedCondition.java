package com.atlassian.plugin.connect.plugin.web.condition;

import java.util.Map;

import com.atlassian.plugin.connect.plugin.lifecycle.upm.LicenseRetriever;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;

public class IsLicensedCondition extends AbstractConnectCondition
{
    private final LicenseRetriever licenseRetriever;

    public IsLicensedCondition(LicenseRetriever licenseRetriever)
    {
        this.licenseRetriever = licenseRetriever;
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> CONTEXT_NOT_USED)
    {
        Option<PluginLicense> licenseOption = licenseRetriever.getLicense(addonKey);
        return licenseOption.isDefined() && licenseOption.get().isActive();
    }
}
