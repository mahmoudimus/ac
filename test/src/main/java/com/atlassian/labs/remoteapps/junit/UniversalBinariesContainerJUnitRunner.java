package com.atlassian.labs.remoteapps.junit;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Lists.*;

/**
 * A simple JUnit runner to start the Universal Binaries container for tests.
 *
 * @see UniversalBinaries
 */
public final class UniversalBinariesContainerJUnitRunner extends BlockJUnit4ClassRunner
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Class<?> testClass;
    private final String[] apps;
    private final PluginInstaller pluginInstaller;

    public UniversalBinariesContainerJUnitRunner(final Class<?> testClass) throws InitializationError
    {
        super(testClass);
        this.testClass = checkNotNull(testClass);
        this.apps = checkApps(toAbsolutePaths(testClass.getAnnotation(UniversalBinaries.class).value()));
        this.pluginInstaller = newInstaller();
    }

    private static String[] checkApps(String[] apps)
    {
        for (String app : apps)
        {
            checkApp(app);
        }
        return apps;
    }

    private static void checkApp(String app)
    {
        if (!new File(app).exists())
        {
            throw new IllegalStateException("Could not find app at '" + app + "'");
        }
    }

    private PluginInstaller newInstaller()
    {
        final Mode pluginMode = Mode.get(testClass.getAnnotation(UniversalBinaries.class));
        return pluginMode.equals(Mode.INSTALL) ? new InstalledPluginInstaller() : new ContainerPluginInstaller();
    }

    private String[] toAbsolutePaths(String[] paths)
    {
        final String moduleDir = findModuleDir();
        return Lists.transform(newArrayList(paths), new Function<String, String>()
        {
            @Override
            public String apply(String input)
            {
                return input.replaceAll("\\$\\{moduleDir\\}", moduleDir);
            }
        }).toArray(new String[paths.length]);
    }

    private String findModuleDir()
    {
        final File packageDir = new File(testClass.getResource("").getFile());
        return findDirWithPom(packageDir);
    }

    private String findDirWithPom(File packageDir)
    {
        if (!packageDir.isDirectory())
        {
            return findDirWithPom(packageDir.getParentFile());
        }

        if (packageDir.equals(new File("/")))
        {
            throw new IllegalStateException("Could not find module directory, went up to /");
        }

        if (new File(packageDir, "pom.xml").exists())
        {
            return packageDir.getAbsolutePath();
        }

        return findDirWithPom(packageDir.getParentFile());
    }

    @Override
    protected Statement withBeforeClasses(Statement statement)
    {
        final Statement next = super.withBeforeClasses(statement);
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                try
                {
                    pluginInstaller.start(apps);
                }
                finally
                {
                    next.evaluate();
                }
            }
        };
    }

    @Override
    protected Statement withAfterClasses(Statement statement)
    {
        final Statement next = super.withAfterClasses(statement);
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                try
                {
                    next.evaluate();
                }
                finally
                {
                    pluginInstaller.stop();
                }
            }
        };
    }
}
