package com.atlassian.labs.remoteapps.spi.descriptor;

import com.atlassian.labs.remoteapps.spi.schema.Schema;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;

/**
 * Module descriptor factory that provides an XML schema
 */
public interface DescribedModuleDescriptorFactory extends ListableModuleDescriptorFactory
{
    // todo: put in ListableModuleDescriptorFactory
    Iterable<String> getModuleDescriptorKeys();

    Schema getSchema(String type);
}
