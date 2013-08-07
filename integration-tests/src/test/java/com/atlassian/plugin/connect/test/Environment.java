package com.atlassian.plugin.connect.test;

public interface Environment
{
    String getEnv(String name);

    String getOptionalEnv(String name, String def);

    void setEnv(String name, String value);

    void setEnvIfNull(String name, String value);
}