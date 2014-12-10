package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;

import javax.annotation.Nonnull;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
public interface AddOnPropertyService
{
    AddOnProperty getPropertyValue(@Nonnull String addonKey,@Nonnull String propertyKey);

    void setPropertyValue(@Nonnull String addonKey, String propertyKey, String value); //TODO: Result instead of void

}
