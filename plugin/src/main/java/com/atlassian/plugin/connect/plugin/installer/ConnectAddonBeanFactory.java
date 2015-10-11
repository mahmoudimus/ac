package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;

/**
 * Validates and unmarshalls Connect add-on JSON descriptors.
 */
public interface ConnectAddonBeanFactory
{

    ConnectAddonBean fromJson(String jsonDescriptor) throws InvalidDescriptorException;

    void remove(String jsonDescriptor);

    void removeAll();
}
