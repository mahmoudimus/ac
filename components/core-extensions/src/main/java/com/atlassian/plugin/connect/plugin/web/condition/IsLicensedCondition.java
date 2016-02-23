package com.atlassian.plugin.connect.plugin.web.condition;

import java.util.Map;

import com.atlassian.plugin.connect.api.web.condition.AbstractConnectCondition;
import com.atlassian.upm.api.license.RemotePluginLicenseService;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;

public class IsLicensedCondition extends AbstractConnectCondition {
    private final RemotePluginLicenseService remotePluginLicenseService;

    public IsLicensedCondition(RemotePluginLicenseService remotePluginLicenseService) {
        this.remotePluginLicenseService = remotePluginLicenseService;
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> CONTEXT_NOT_USED) {
        Option<PluginLicense> licenseOption = remotePluginLicenseService.getRemotePluginLicense(addonKey);
        return licenseOption.isDefined() && licenseOption.get().isActive();
    }
}
