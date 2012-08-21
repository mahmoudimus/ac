package com.atlassian.labs.remoteapps.junit;

import com.atlassian.labs.remoteapps.container.Main;

final class ContainerPluginInstaller implements PluginInstaller
{
    private Main container;

    @Override
    public void start(String... apps)
    {
        if (container != null)
        {
            return; // already started
        }

        try
        {
            container = new Main(apps);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void stop()
    {
        if (container != null)
        {
            container.stop();
        }
    }
}
