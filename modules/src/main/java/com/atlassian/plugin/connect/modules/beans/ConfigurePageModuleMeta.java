package com.atlassian.plugin.connect.modules.beans;

public class ConfigurePageModuleMeta extends ConnectModuleMeta<ConnectPageModuleBean>
{

    public ConfigurePageModuleMeta()
    {
        super("configurePage", ConnectPageModuleBean.class);
    }

    @Override
    public boolean multipleModulesAllowed()
    {
        return false;
    }
}
