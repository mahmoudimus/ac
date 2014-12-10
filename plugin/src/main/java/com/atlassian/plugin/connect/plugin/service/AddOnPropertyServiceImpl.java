package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
@Component
public class AddOnPropertyServiceImpl implements AddOnPropertyService
{
    public static final int MAXIMUM_VALUE_LENGTH = 255;

    private final AddOnPropertyStore store;

    @Autowired
    public AddOnPropertyServiceImpl(final AddOnPropertyStore store) {this.store = checkNotNull(store);}

    @Override
    public AddOnProperty getPropertyValue(@Nonnull final String addonKey, @Nonnull final String propertyKey)
    {
        return store.getPropertyValue(addonKey, propertyKey);
    }

    @Override
    public void setPropertyValue(@Nonnull final String addonKey, final String propertyKey, final String value)
    {
        store.setPropertyValue(addonKey, propertyKey, value);
    }
}
