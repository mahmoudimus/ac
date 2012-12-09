package com.atlassian.plugin.remotable.junit;

import com.atlassian.plugin.remotable.container.Main;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

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
            final ClassLoader classLoader = getContainerClassLoader();

            // getting all the classes, methods first
            final Class<?> mainClass = loadMainClass(classLoader);
            final Method newMainMethod = getMethod(mainClass, "newMain", String[].class);
            final Method stopMethod = getMethod(mainClass, "stop");


            // using null for a static method
            final Object container = invoke(null, newMainMethod, new Object[]{key.apps});

            LOGGER.info("Started container with apps: {}", Arrays.toString(key.apps));

            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                public void run()
                {
                    LOGGER.info("Stopping container with apps: {}", Arrays.toString(key.apps));
                    invoke(container, stopMethod);
                }
            });
            return container;
        }

        private Method getMethod(Class<?> aClass, String name, Class<?>... args)
        {
            try
            {
                return checkNotNull(aClass).getMethod(name, args);
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Could not find method " + aClass.getName() + "#" + name + "(" + Arrays.toString(args) + ")", e);
            }
        }

        private Object invoke(Object object, Method method, Object... args)
        {
            try
            {
                return method.invoke(object, args);
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Could not invoke method " + method + " on object " + object, e);
            }
        }

        private Class<?> loadMainClass(ClassLoader classLoader)
        {
            final String mainClass = Main.class.getName();
            try
            {
                return classLoader.loadClass(mainClass);
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Could not load main class, " + mainClass);
            }
        }

        private URLClassLoader getContainerClassLoader()
        {
            final String containerPath = "/remotable-plugins-container-standalone.jar";
            final URL containerJar = getClass().getResource(containerPath);
            if (containerJar == null)
            {
                throw new IllegalStateException("Could not find container! Looked at classpath:" + containerPath);
            }
            return new URLClassLoader(new URL[]{containerJar});
        }
    }
}
