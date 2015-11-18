package com.atlassian.plugin.connect.plugin.descriptor.event;

import com.atlassian.analytics.api.annotations.EventName;
import com.atlassian.plugin.connect.spi.descriptor.ConnectModuleValidationException;

@EventName("connect.addon.moduleValidation.failedAfterInstall")
public class ConnectAddonModuleValidationFailedAfterInstallEvent
{

    private final String addonKey;
    private final String moduleType;
    private final String message;

    public ConnectAddonModuleValidationFailedAfterInstallEvent(ConnectModuleValidationException e)
    {
        addonKey = e.getAddon().getKey();
        moduleType = e.getModuleMeta().getDescriptorKey();
        message = e.getMessage();
    }

    public String getAddonKey()
    {
        return addonKey;
    }

    public String getModuleType()
    {
        return moduleType;
    }

    public String getMessage()
    {
        return message;
    }
}
