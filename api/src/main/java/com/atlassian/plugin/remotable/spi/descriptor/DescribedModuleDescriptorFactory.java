package com.atlassian.plugin.remotable.spi.descriptor;

import com.atlassian.plugin.remotable.spi.schema.Schema;
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
