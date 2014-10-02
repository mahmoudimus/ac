package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.json.schema.model.JsonSchema;
import com.atlassian.marketplace.client.impl.JsonEntityEncoding;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

import java.util.List;

class ConnectModuleThingy {} // ignore




interface ConnectModuleDescriptorProvider<T>
{
    List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, List<T> modules);
}

interface ConnectModuleProvider<T>
{
    JsonSchema getSchema();

    ModuleDeserialiserProvider<T> getDeserialiserProvider();

    ConnectModuleDescriptorProvider<T> getModuleDescriptorProvider();
}


interface ModuleDeserialiser<T>
{
    T deserialise(Class<? extends T> cls);
}

// Note the purpose of the two interfaces for deserialising is an attempt
// to get around classloader issues. Not sure if
// it would work nor whether it is needed
interface ModuleDeserialiserProvider<T>
{
    T deserialise(ModuleDeserialiser<T> m);
}

class DefaultModuleDeserialiser<T> implements ModuleDeserialiser<T>
{
    private JsonDeserializationContext context;
    private JsonElement json;

    @Override
    public T deserialise(Class<? extends T> cls)
    {
        return context.deserialize(json, cls);
    }
}

class DefaultModuleDeserialiserProvider<T> implements ModuleDeserialiserProvider<T>
{
    private final Class<? extends T> moduleClass;

    public DefaultModuleDeserialiserProvider(Class<? extends T> moduleClass)
    {
        this.moduleClass = moduleClass;
    }

    @Override
    public T deserialise(ModuleDeserialiser<T> m)
    {
        return m.deserialise(moduleClass);
    }
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