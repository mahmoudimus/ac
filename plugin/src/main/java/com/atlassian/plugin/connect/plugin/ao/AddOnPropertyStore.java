package com.atlassian.plugin.connect.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.google.common.collect.Iterables;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
@Component
public class AddOnPropertyStore
{
    private final ActiveObjects ao;

    @Autowired
    public AddOnPropertyStore(final ActiveObjects ao) {this.ao = checkNotNull(ao);}

    public AddOnProperty getPropertyValue(@Nonnull final String addonKey, @Nonnull final String propertyKey)
    {
        AddOnPropertyAO[] properties = ao.find(AddOnPropertyAO.class, Query.select().where("KEY = ?", propertyKey));
        AddOnPropertyAO first = Iterables.getFirst(Arrays.asList(properties), null);
        if (first != null)
            return new AddOnProperty(first.getPropertyKey(),first.getValue());
        return null;
    }

    public void setPropertyValue(@Nonnull final String addonKey, @Nonnull final String propertyKey, final String value)
    {
        AddOnPropertyAO property = ao.create(AddOnPropertyAO.class);
        property.setPluginKey(addonKey);
        property.setPropertyKey(propertyKey);
        property.setValue(value);

        property.setPrimaryKey(String.format("%s:%s", addonKey, propertyKey));
        property.save();
    }
}

