package com.atlassian.plugin.remotable.container;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public final class CommandLineContainerConfiguration implements ContainerConfiguration
{
    private static final String MAVEN_TARGET_DIRECTORY = "target";
    private static final String CACHE_DIRECTORY_NAME = ".cache";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Iterable<String> applicationsPaths;
    private final File cacheDirectory;

    public CommandLineContainerConfiguration(Iterable<String> applicationsPaths)
    {
        this.applicationsPaths = ImmutableList.copyOf(applicationsPaths);
        this.cacheDirectory = getDefaultCacheDirectory();
    }

    @Override
    public Iterable<String> getApplicationsPaths()
    {
        return applicationsPaths;
    }

    @Override
    public File getCacheDirectory()
    {
        return cacheDirectory;
    }

    @Override
    public File getCacheDirectory(String path)
    {
        return ContainerUtils.mkdirs(new File(getCacheDirectory(), path));
    }

    private File getDefaultCacheDirectory()
    {
        final File target = new File(MAVEN_TARGET_DIRECTORY);
        final File cacheDirectory;
        if (target.exists())
        {
            logger.debug("Maven target directory exists at '" + target.getAbsolutePath() + "'.");
            logger.debug("Setting cache directory inside that target directory");
            cacheDirectory = new File(target, CACHE_DIRECTORY_NAME);
        }
        else
        {
            cacheDirectory = new File(CACHE_DIRECTORY_NAME);
        }
        logger.debug("Container cache directory set to '" + cacheDirectory.getAbsolutePath() + "'");
        return cacheDirectory;
    }
}
