package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;

public interface Module
{
    BaseModuleBean toBean(Class<? extends BaseModuleBean> moduleClass);
}