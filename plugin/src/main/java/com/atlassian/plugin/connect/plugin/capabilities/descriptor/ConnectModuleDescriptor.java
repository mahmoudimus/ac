package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

public interface ConnectModuleDescriptor<T> extends ModuleDescriptor<T>
{
    void setAddonKey(String addonKey);
}
