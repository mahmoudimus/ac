package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

/**
 * Provides extra validation for JSON descriptors that can't be expressed in json-schema.
 *
 * @since 1.0
 */
public interface AddonBeanValidator
{
    void validate(ConnectAddonBean addonBean) throws InvalidDescriptorException;
}
