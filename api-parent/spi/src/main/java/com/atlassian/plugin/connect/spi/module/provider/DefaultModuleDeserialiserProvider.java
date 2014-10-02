package com.atlassian.plugin.connect.spi.module.provider;

public class DefaultModuleDeserialiserProvider implements ModuleDeserialiserProvider
{
    private final Class<?> moduleClass;

    public DefaultModuleDeserialiserProvider(Class<? > moduleClass)
    {
        this.moduleClass = moduleClass;
    }

    @Override
    public Object deserialise(ModuleDeserialiser m)
    {
        return m.deserialise(moduleClass);
    }
}
