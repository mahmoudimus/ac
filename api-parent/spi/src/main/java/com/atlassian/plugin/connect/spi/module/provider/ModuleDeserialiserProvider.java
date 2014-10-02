package com.atlassian.plugin.connect.spi.module.provider;

// Note the purpose of the two interfaces for deserialising is an attempt
// to get around classloader issues. Not sure if
// it would work nor whether it is needed
public interface ModuleDeserialiserProvider
{
    Object deserialise(ModuleDeserialiser m);
}
