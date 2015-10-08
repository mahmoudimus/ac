package com.atlassian.plugin.connect.spi.module;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;

import java.util.List;

/**
 * A plugin module descriptor interface for providers of feature modules for Atlassian Connect.
 *
 * A module provider defines an entry in the <pre>modules</pre> field of the Atlassian Connect add-on JSON descriptor.
 * The provider is responsible for validating the syntax and semantics of this descriptor entry, as well as for
 * mapping each descriptor module into one or several plugin modules.
 *
 * @param <T> the type of the add-on descriptor module representation
 */
public interface ConnectModuleProvider<T extends BaseModuleBean>
{

    /**
     * Returns the metadata for this module.
     *
     * @return the module metadata
     */
    ConnectModuleMeta<T> getMeta();

    /**
     * Validates the syntax and semantics of the given module list entry and deserializes it into a list of module beans.
     *
     * The module list entry is a valid JSON element (object or array)
     *
     * @param jsonModuleListEntry the string representation of the module list entry JSON element
     * @param descriptor the add-on descriptor (without the module list)
     * @return the module beans deserialized from the module list entry
     * @throws ConnectModuleValidationException if the syntax or semantics of the module list entry is invalid
     */
    List<T> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor)
            throws ConnectModuleValidationException;

    /**
     * Creates plugin module descriptors representing the given list of add-on modules.
     *
     * @param modules the add-on modules for which to create plugin module descriptors
     * @param moduleProviderContext
     * @return the plugin module descriptors created
     */
    List<ModuleDescriptor> createPluginModuleDescriptors(List<T> modules, ConnectModuleProviderContext moduleProviderContext);
}
