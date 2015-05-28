package com.atlassian.plugin.connect.core.capabilities.validate;

import com.atlassian.plugin.connect.core.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

/**
 * Provides extra validation for JSON descriptors that can't be expressed in json-schema.
 *
 * @since 1.0
 */
public interface AddOnBeanValidator
{
    void validate(ConnectAddonBean addonBean) throws InvalidDescriptorException;
}
