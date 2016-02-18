package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;

/**
 * Provides extra validation for shallow JSON descriptors that can't be expressed in json-schema.
 */
public interface ShallowConnectAddonBeanValidator
{

    void validate(ShallowConnectAddonBean addonBean) throws InvalidDescriptorException;
}
