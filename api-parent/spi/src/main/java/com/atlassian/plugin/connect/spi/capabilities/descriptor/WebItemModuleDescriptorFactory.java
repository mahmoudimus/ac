package com.atlassian.plugin.connect.spi.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

public interface WebItemModuleDescriptorFactory extends ConnectModuleDescriptorFactory<WebItemModuleBean,WebItemModuleDescriptor>
{
    public static final String DIALOG_OPTION_PREFIX = "-acopt-";

    public WebItemModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin,
            WebItemModuleBean bean, Class<? extends Condition> additionalCondition);

    public WebItemModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin,
            WebItemModuleBean bean, Iterable<Class<? extends Condition>> additionalConditions);
}
