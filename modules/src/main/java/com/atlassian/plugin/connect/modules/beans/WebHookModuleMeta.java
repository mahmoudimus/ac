package com.atlassian.plugin.connect.modules.beans;

public class WebHookModuleMeta implements ConnectModuleMeta<WebHookModuleBean>
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
    public Class<WebHookModuleBean> getBeanClass()
    {
        return WebHookModuleBean.class;
    }
}
