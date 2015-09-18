package com.atlassian.plugin.connect.modules.beans;

public class WebHookModuleMeta implements ConnectModuleMeta
{
    @Override
    public boolean multipleModulesAllowed()
    {
        return true;
    }

    @Override
    public String getDescriptorKey()
    {
        return "webhooks";
    }

    @Override
    public Class getBeanClass()
    {
        return WebHookModuleBean.class;
    }
}
