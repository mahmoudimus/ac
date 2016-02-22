package com.atlassian.plugin.connect.spi.lifecycle;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;

import java.util.List;

/**
 * A plugin module descriptor interface for providers of feature modules for Atlassian Connect.
 *
 * A module provider defines an entry in the <pre>modules</pre> field of the Atlassian Connect add-on JSON descriptor.
 * The provider is responsible for validating the syntax and semantics of this descriptor entry, as well as for
 * mapping each descriptor module into one or more plugin modules.
 *
 * Descriptor modules are validated both as part of add-on installation and add-on enablement (also performed during
 * start-up of Atlassian Connect). Throwing a {@link ConnectModuleValidationException} for a failure to deserialize a
 * module list entry or to validate it syntactically or semantically interrupts those processes.
 *
 * - Installation: The process fails and an error is returned to the Universal Plugin Manager.
 * - Enablement: The process fails and the add-on is skipped. Enablement is retried the next time Atlassian Connect
 * starts up.
 *
 * @param <T> the type of the add-on descriptor module representation
 */
public interface ConnectModuleProvider<T extends BaseModuleBean> {

    /**
     * Returns the metadata for this module.
     *
     * @return the module metadata
     */
    ConnectModuleMeta<T> getMeta();

    /**
     * Validates the syntax and semantics of the given module list entry and deserializes it into a list of module beans.
     *
     * The module list entry is a valid JSON element (object or array).
     *
     * This method is called <b>before</b> {@link #deserializeAddonDescriptorModules(String, ShallowConnectAddonBean)}.
     *
     * @param jsonModuleListEntry the string representation of the module list entry JSON element
     * @param descriptor          the add-on descriptor (without the module list)
     * @return the module beans deserialized from the module list entry
     * @throws ConnectModuleValidationException if the syntax or semantics of the module list entry are invalid
     */
    List<T> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor)
            throws ConnectModuleValidationException;

    /**
     * Validates the semantics of any dependencies between this module and other modules.
     *
     * This method is called <b>after</b> {@link #deserializeAddonDescriptorModules(String, ShallowConnectAddonBean)}.
     *
     * @param descriptor the add-on descriptor (with the module list)
     * @throws ConnectModuleValidationException if the semantics of the dependencies of this module list entry are invalid
     */
    void validateModuleDependencies(ConnectAddonBean descriptor) throws ConnectModuleValidationException;

    /**
     * Creates plugin module descriptors representing the given list of add-on modules.
     *
     * @param modules the add-on modules for which to create plugin module descriptors
     * @param addon   the add-on descriptor
     */
    List<ModuleDescriptor> createPluginModuleDescriptors(List<T> modules, ConnectAddonBean addon);
}
