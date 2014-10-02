package com.atlassian.plugin.connect.spi.module.provider;

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
