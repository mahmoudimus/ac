package com.atlassian.plugin.remotable.spi.module;

/**
 * This class exists solely to allow plugins being installed to generate the correct package imports.
 * The plugin will place objects that need to be resolved by the universal binaries as module descriptor
 * module classes.
 */
public final class ModuleMarker
{
    private ModuleMarker(){}
}
