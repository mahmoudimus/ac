package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.spi.module.ConnectModuleValidationException;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;

public interface AvailableModuleTypes
{
    boolean validModuleType(String moduleType);

    boolean multipleModulesAllowed(String moduleType);

    List<ModuleBean> deserializeModulesOfSameType(Map.Entry<String, JsonElement> modules) throws ConnectModuleValidationException;
}
