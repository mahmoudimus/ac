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
        Object bean = m.deserialise(moduleClass);
        validate(bean);
        return bean;
    }

    protected void validate(Object bean)
    {
        // any additional validation can go here
    }
}
