package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;

import java.util.Map;

/**
 * Validates and unmarshalls Connect addon JSON descriptors.
 *
 * @since 1.0
 */
public interface ConnectAddonBeanFactory
{
    ConnectAddonBean fromJson(String jsonDescriptor) throws InvalidDescriptorException;

    ConnectAddonBean fromJson(String jsonDescriptor,Map<String,String> i18nCollector) throws InvalidDescriptorException;

    ConnectAddonBean fromJsonSkipValidation(String jsonDescriptor);
    
    ConnectAddonBean fromJsonSkipValidation(String jsonDescriptor,Map<String,String> i18nCollector);
}
