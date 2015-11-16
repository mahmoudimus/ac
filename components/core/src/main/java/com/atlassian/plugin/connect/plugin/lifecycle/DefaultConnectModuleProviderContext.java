package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.api.web.item.ModuleLocationQualifier;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleProviderContext;

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
