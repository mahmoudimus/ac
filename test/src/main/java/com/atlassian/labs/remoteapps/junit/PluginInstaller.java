package com.atlassian.labs.remoteapps.junit;

interface PluginInstaller
{
    void start(String... apps);

    void stop();
}
