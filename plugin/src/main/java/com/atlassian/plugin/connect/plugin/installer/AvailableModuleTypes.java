package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleValidationException;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;

public interface AvailableModuleTypes
{
    abstract boolean validModuleType(String moduleType);

    abstract boolean multipleModulesAllowed(String moduleType);

    abstract List<ModuleBean> deserializeModulesOfSameType(Map.Entry<String, JsonElement> modules) throws ConnectModuleValidationException;
}
