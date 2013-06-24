package com.atlassian.plugin.remotable.spi.permission;

import java.io.File;
import java.net.URL;
import java.util.jar.Manifest;

/**
 *
 */
public interface PluginScanner
{
    Manifest getManifest();

    File getFile();

    Iterable<String> getEntryPaths();

    URL getEntry(String path);
}
