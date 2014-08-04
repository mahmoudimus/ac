package com.atlassian.plugin.connect.plugin.module.provider;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;

import java.util.List;

public interface ModuleListProviderFactory
{
    List<DescriptorToRegister> getDescriptors(final ConnectAddonBean addon, final BeanTransformContext ctx);
}
