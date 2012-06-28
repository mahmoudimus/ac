package com.atlassian.labs.remoteapps.apputils;

/**
 * Abstraction on top of environment variables and some sort of simple
 * storage capability.
 */
public interface Environment
{
    String getEnv(String name);

    String getOptionalEnv(String name, String def);

    void setEnv(String name, String value);

    void setEnvIfNull(String name, String value);
}
