package com.atlassian.plugin.connect.plugin.web.condition;

import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.lifecycle.upm.LicenseRetriever;
import com.atlassian.plugin.web.Condition;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;

@ConnectCondition
public class IsLicensedCondition implements Condition
{
    private final LicenseRetriever licenseRetriever;

    private String addonKey;

    public IsLicensedCondition(LicenseRetriever licenseRetriever)
    {
        this.licenseRetriever = licenseRetriever;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        Optional<String> maybeAddonKey = ConnectConditionContext.from(params).getAddonKey();
        if (!maybeAddonKey.isPresent())
        {
            throw new IllegalStateException("Condition should have been invoked in the Atlassian Connect context, but apparently it was not, add-on key is missing");
        }
        this.addonKey = maybeAddonKey.get();
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> CONTEXT_NOT_USED)
    {
        Option<PluginLicense> licenseOption = licenseRetriever.getLicense(addonKey);
        return licenseOption.isDefined() && licenseOption.get().isActive();
    }
}
