package com.atlassian.plugin.connect.modules.beans;

public class WebPanelModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "webPanels";
    }

    @Override
    public Class getBeanClass()
    {
        return WebPanelModuleBean.class;
    }
}
