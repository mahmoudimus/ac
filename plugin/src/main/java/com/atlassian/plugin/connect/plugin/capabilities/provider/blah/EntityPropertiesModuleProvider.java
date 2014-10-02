package com.atlassian.plugin.connect.plugin.capabilities.provider.blah;

import com.atlassian.json.schema.model.JsonSchema;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.*;

public class EntityPropertiesModuleProvider implements ConnectModuleProvider<EntityPropertyModuleBean>
{
    private ModuleDeserialiserProvider deserialiserProvider =
            new DefaultModuleDeserialiserProvider(EntityPropertyModuleBean.class);

    @Override
    public JsonSchema getSchema()
    {
        return null;
    }

    @Override
    public ModuleDeserialiserProvider getDeserialiserProvider()
    {
        return deserialiserProvider;
    }

    @Override
    public ConnectModuleDescriptorProvider<EntityPropertyModuleBean> getModuleDescriptorProvider()
    {
        return null;
    }
}
