package com.atlassian.plugin.connect.plugin.capabilities.module.serialise;

import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;

// a hack for GSON
class DummyModuleList extends ModuleList
{

    public DummyModuleList()
    {
    }

    public DummyModuleList(BaseModuleBeanBuilder builder)
    {
        super(builder);
    }
}
