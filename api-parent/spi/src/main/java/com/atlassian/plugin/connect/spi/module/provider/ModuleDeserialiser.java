package com.atlassian.plugin.connect.spi.module.provider;

public interface ModuleDeserialiser
{
    Object deserialise(Class<?> cls);
}
