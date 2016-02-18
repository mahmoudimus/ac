package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

/**
 * Delegates to {@link AddonBeanValidator} implementations to provide extra validation for JSON descriptors that can't
 * easily be expressed in json-schema.
 *
 * @since 1.0
 */
public interface AddonBeanValidatorService
{
    void validate(ConnectAddonBean addonBean) throws InvalidDescriptorException;
}
