package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

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