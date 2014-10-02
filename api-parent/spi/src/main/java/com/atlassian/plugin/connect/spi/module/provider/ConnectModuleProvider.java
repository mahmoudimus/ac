package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.json.schema.model.JsonSchema;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

import java.util.List;




interface ConnectModuleDescriptorProvider<T>
{
    List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, List<T> modules);
}



public interface ConnectModuleProvider<T>
{
    JsonSchema getSchema();

    ModuleDeserialiserProvider getDeserialiserProvider();

    ConnectModuleDescriptorProvider<T> getModuleDescriptorProvider();
}


/**
 * A context for ConnectModuleProviders.
 *
 * Provides data and helpers that are shared across modules for a single
 * addon bean
 */
interface ConnectModuleProviderContext
{
    /**
     * The connect addon that owns the module being processed
     */
    ConnectAddonBean getConnectAddonBean();

    /**
     * A helper for qualifying location names so they match the qualified names of module keys
     */
    ModuleLocationQualifier getLocationQualifier();
}

/**
 * A utility that qualifies any locations (and location segments) that match keys in web items or web sections.
 *
 * Connect requires all module keys to be unique (at least within a page). To ensure this, connect qualifies
 * module keys with the addon key as a prefix.
 *
 * However, the keys of web items and web sections can be used within location fields of web items and sections.
 * As such, these key references must also be qualified so that they match when it hits the P2 system.
 * See AC-1201 for details.
 *
 * Note that locations may have more than one segment separated by a '/'. Each one of these may be a module key.
 * e.g. a web item may have a location like "top-menu/first-section" where "top-menu" is the key of another web item
 * and "first-section" is the key of a web section.
 */
interface ModuleLocationQualifier
{
    /**
     * Process the given location, by substituting any location segments that are keys to other modules with their
     * qualified name
     * @param location the location to process
     * @return the possibly qualified location
     */
    String processLocation(String location);
}


