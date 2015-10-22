package com.atlassian.plugin.connect.modules.beans;

public class WebHookModuleMeta extends ConnectModuleMeta<WebHookModuleBean>
{

    public WebHookModuleMeta()
    {
        super("webhooks", WebHookModuleBean.class);
    }
}
