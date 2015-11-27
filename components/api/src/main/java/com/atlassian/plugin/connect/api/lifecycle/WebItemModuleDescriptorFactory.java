package com.atlassian.plugin.connect.api.lifecycle;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

public interface WebItemModuleDescriptorFactory extends ConnectModuleDescriptorFactory<WebItemModuleBean,WebItemModuleDescriptor>
{

    public static final String DIALOG_OPTION_PREFIX = "-acopt-";

    public WebItemModuleDescriptor createModuleDescriptor(WebItemModuleBean bean, ConnectAddonBean addon, Plugin plugin,
            Class<? extends Condition> additionalCondition);

    public WebItemModuleDescriptor createModuleDescriptor(WebItemModuleBean bean, ConnectAddonBean addon, Plugin plugin,
            Iterable<Class<? extends Condition>> additionalConditions);
}
