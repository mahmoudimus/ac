package com.atlassian.plugin.connect.modules.beans;

public class WebPanelModuleMeta implements ConnectModuleMeta<WebPanelModuleBean>
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
    public Class<WebPanelModuleBean> getBeanClass()
    {
        return WebPanelModuleBean.class;
    }
}
