package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.json.schema.model.JsonSchema;

import static org.junit.Assert.*;

public class ConnectModuleProviderTest
{

}


class MyModuleDeserialiserProviderImpl extends DefaultModuleDeserialiserProvider<MyModule>
{
    public MyModuleDeserialiserProviderImpl()
    {
        super(MyModule.class);
    }
}

class MyModule
{
    String key;
}


class MyModuleProvider implements ConnectModuleProvider<MyModule>
{
    @Override
    public JsonSchema getSchema()
    {
        return null;
    }

    @Override
    public ModuleDeserialiserProvider<MyModule> getDeserialiserProvider()
    {
        return new MyModuleDeserialiserProviderImpl();
    }

    @Override
    public ConnectModuleDescriptorProvider<MyModule> getModuleDescriptorProvider()
    {
        return null;
    }

}


