package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;

import java.util.Map;

/**
 * Validates and unmarshalls Connect addon JSON descriptors.
 *
 * @since 1.0
 */
public interface ConnectAddonBeanFactory
{
    <M extends ModuleList> ConnectAddonBean<M> fromJson(String jsonDescriptor) throws InvalidDescriptorException;

    <M extends ModuleList> ConnectAddonBean<M> fromJson(String jsonDescriptor,Map<String,String> i18nCollector) throws InvalidDescriptorException;

    <M extends ModuleList> ConnectAddonBean<M> fromJsonSkipValidation(String jsonDescriptor);

    <M extends ModuleList> ConnectAddonBean<M> fromJsonSkipValidation(String jsonDescriptor,Map<String,String> i18nCollector);
}
