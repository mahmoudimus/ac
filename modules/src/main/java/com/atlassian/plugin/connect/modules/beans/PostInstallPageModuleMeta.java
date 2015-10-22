package com.atlassian.plugin.connect.modules.beans;

public class PostInstallPageModuleMeta extends ConnectModuleMeta<ConnectPageModuleBean>
{

    public PostInstallPageModuleMeta()
    {
        super("postInstallPage", ConnectPageModuleBean.class);
    }

    @Override
    public boolean multipleModulesAllowed()
    {
        return false;
    }
}
