package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.spi.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.plugin.capabilities.provider.ModuleLocationQualifier;

public class DefaultConnectModuleProviderContext implements ConnectModuleProviderContext
{
    private final ConnectAddonBean addonBean;
    private final DefaultModuleLocationQualifier locationQualifier;

    public DefaultConnectModuleProviderContext(ConnectAddonBean addonBean)
    {
        this.addonBean = addonBean;
        this.locationQualifier = new DefaultModuleLocationQualifier(addonBean);
    }

    @Override
    public ConnectAddonBean getConnectAddonBean()
    {
        return addonBean;
    }

    @Override
    public ModuleLocationQualifier getLocationQualifier()
    {
        return locationQualifier;
    }

}
