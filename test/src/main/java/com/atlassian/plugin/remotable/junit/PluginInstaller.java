package com.atlassian.plugin.remotable.junit;

interface PluginInstaller
{
    void start(String... apps);

    void stop();
}
