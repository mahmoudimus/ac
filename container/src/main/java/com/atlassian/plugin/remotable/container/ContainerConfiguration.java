package com.atlassian.plugin.remotable.container;

import java.io.File;

public interface ContainerConfiguration
{
    /**
     * Gets the <em>list</em> of applications to load in the container.
     */
    Iterable<String> getApplicationsPaths();

    /**
     * Gets the absolute or relative path to the cache directory of the container.
     * Defaults to relative directory {@code .cache}
     */
    File getCacheDirectory();

    /**
     * Gets a directory relative to the cache directory. The directory will be created before it is returned.
     */
    File getCacheDirectory(String path);
}
