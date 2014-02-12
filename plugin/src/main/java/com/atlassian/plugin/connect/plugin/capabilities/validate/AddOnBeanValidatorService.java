package com.atlassian.plugin.connect.plugin.capabilities.validate;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;

/**
 * Delegates to {@link AddOnBeanValidator} implementations to provide extra validation for JSON descriptors that can't
 * easily be expressed in json-schema.
 *
 * @since 1.0
 */
public interface AddOnBeanValidatorService
{
    void validate(ConnectAddonBean addonBean) throws InvalidDescriptorException;
}
