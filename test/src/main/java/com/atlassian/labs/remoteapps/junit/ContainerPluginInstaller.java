package com.atlassian.labs.remoteapps.junit;

import com.atlassian.labs.remoteapps.container.Main;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import static com.google.common.base.Preconditions.*;

final class ContainerPluginInstaller implements PluginInstaller
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerPluginInstaller.class);

    private static final Cache<ContainerConfiguration, Object> CONTAINERS = CacheBuilder.newBuilder().build(new ContainerCacheLoader());

    @Override
    public void start(String... apps)
    {
        CONTAINERS.getUnchecked(new ContainerConfiguration(apps));
    }

    @Override
    public void stop()
    {
        // do nothing!
    }

    private static final class ContainerConfiguration
    {
        public final String[] apps;

        private ContainerConfiguration(String[] apps)
        {
            this.apps = checkNotNull(apps);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }

            final ContainerConfiguration other = (ContainerConfiguration) obj;
            return Arrays.equals(this.apps, other.apps);
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode(apps);
        }
    }

    private static class ContainerCacheLoader extends CacheLoader<ContainerConfiguration, Object>
    {
        @Override
        public Object load(final ContainerConfiguration key) throws Exception
        {
            URLClassLoader classLoader = new URLClassLoader(new URL[] {getClass().getResource("/remoteapps-container-standalone.jar")});

            final Class<?> mainClass = classLoader.loadClass(Main.class.getName());
            final Object container = mainClass.getConstructors()[0].newInstance(new Object[]{key.apps});
            LOGGER.info("Started container with apps: {}", Arrays.toString(key.apps));

            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                public void run()
                {
                    LOGGER.info("Stopping container with apps: {}", Arrays.toString(key.apps));
                    try
                    {
                        mainClass.getMethod("stop").invoke(container);
                    }
                    catch (IllegalAccessException e)
                    {
                        e.printStackTrace();  //To change body of catch statement use File |
                        // Settings | File Templates.
                    }
                    catch (InvocationTargetException e)
                    {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    catch (NoSuchMethodException e)
                    {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            });
            return container;
        }
    }
}
