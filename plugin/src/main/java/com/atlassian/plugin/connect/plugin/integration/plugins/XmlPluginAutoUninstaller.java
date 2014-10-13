package com.atlassian.plugin.connect.plugin.integration.plugins;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;

import javax.inject.Inject;
import javax.inject.Named;

@ExportAsService(LifecycleAware.class)
@Named
public class XmlPluginAutoUninstaller implements LifecycleAware
{

    private final XmlPluginAutoUninstallHelper helper;

    @Inject
    public XmlPluginAutoUninstaller(XmlPluginAutoUninstallHelper helper)
    {
        this.helper = helper;
    }

    @Override
    public void onStart()
    {
        helper.uninstallXmlPlugins();
    }

}
