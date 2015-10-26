package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.spi.descriptor.ConnectModuleValidationException;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;

public interface AvailableModuleTypes
{
    boolean validModuleType(String moduleType);

    boolean multipleModulesAllowed(String moduleType);

    List<ModuleBean> deserializeModules(String moduleTypeKey, JsonElement modules) throws ConnectModuleValidationException;
}
