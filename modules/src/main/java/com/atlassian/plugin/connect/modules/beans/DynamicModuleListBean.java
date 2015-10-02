package com.atlassian.plugin.connect.modules.beans;

import com.google.common.base.Supplier;

import java.util.List;
import java.util.Map;

public class DynamicModuleListBean
{
    private Map<String, Supplier<List<ModuleBean>>> modules;
}
