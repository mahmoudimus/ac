package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import org.osgi.framework.BundleContext;

import java.util.List;

public class DynamicContentMacroModuleProvider implements ConnectModuleProvider<DynamicContentMacroModuleBean>
{
    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<DynamicContentMacroModuleBean> beans)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
