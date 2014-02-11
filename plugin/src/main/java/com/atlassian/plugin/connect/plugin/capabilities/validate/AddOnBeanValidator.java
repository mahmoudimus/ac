package com.atlassian.plugin.connect.plugin.capabilities.validate;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;

/**
 * Provides extra validation for JSON descriptors that can't be expressed in json-schema.
 *
 * @since 1.0
 */
public interface AddOnBeanValidator
{
    void validate(Plugin plugin, ConnectAddonBean addonBean) throws InvalidDescriptorException;
}
