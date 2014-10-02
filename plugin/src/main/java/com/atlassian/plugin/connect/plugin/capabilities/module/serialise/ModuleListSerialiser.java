package com.atlassian.plugin.connect.plugin.capabilities.module.serialise;

import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public interface ModuleListSerialiser extends JsonSerializer<ModuleList>, JsonDeserializer<ModuleList>
{

}
