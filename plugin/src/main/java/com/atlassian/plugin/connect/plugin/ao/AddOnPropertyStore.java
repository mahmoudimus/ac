package com.atlassian.plugin.connect.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public Optional<AddOnProperty> getPropertyValue(@Nonnull final String addonKey, @Nonnull final String propertyKey)
    {
        AddOnPropertyAO[] properties = ao.find(AddOnPropertyAO.class, Query.select().where("PLUGIN_KEY = ? AND PROPERTY_KEY = ?", addonKey, propertyKey));
        AddOnPropertyAO first = Iterables.getFirst(Arrays.asList(properties), null);
        if (first != null)
        {
            return Optional.of(new AddOnProperty(first.getPropertyKey(), first.getValue()));
        }
        return Optional.absent();
    }

    public PutResult setPropertyValue(@Nonnull final String addonKey, @Nonnull final String propertyKey, @Nonnull final String value)
    {
            return ao.executeInTransaction(new TransactionCallback<PutResult>()
            {
                @Override
                public PutResult doInTransaction()
                {
                    if (!existsProperty(addonKey, propertyKey))
                    {
                        AddOnPropertyAO property = ao.create(AddOnPropertyAO.class,
                                new DBParam("PLUGIN_KEY", addonKey),
                                new DBParam("PROPERTY_KEY", propertyKey),
                                new DBParam("VALUE", value),
                                new DBParam("PRIMARY_KEY", getPrimaryKeyForProperty(addonKey, propertyKey)));
                        property.save();
                        return PutResult.PROPERTY_CREATED;

                    }
                    else
                    {
                        AddOnPropertyAO propertyAO = getAddonPropertyForKey(addonKey, propertyKey);
                        propertyAO.setValue(value);
                        propertyAO.save();
                        return PutResult.PROPERTY_UPDATED;
                    }
                }
            });
    }

    private boolean existsProperty(@Nonnull final String addonKey, @Nonnull final String propertyKey)
    {
        AddOnPropertyAO property = ao.get(AddOnPropertyAO.class, getPrimaryKeyForProperty(addonKey, propertyKey));
        return property != null;
    }
    
    public List<String> listProperties(@Nonnull final String addonKey)
    {
        List<String> results = new ArrayList<String>();
        AddOnPropertyAO[] properties = ao.find(AddOnPropertyAO.class, Query.select().where("PLUGIN_KEY = ?", addonKey));
        for (AddOnPropertyAO addOnPropertyAO : properties)
        {
            results.add(addOnPropertyAO.getPropertyKey());
        }
        return results;
    }

    private String getPrimaryKeyForProperty(final String addonKey, final String propertyKey) {return String.format("%s:%s", addonKey, propertyKey);}
    private AddOnPropertyAO getAddonPropertyForKey(final String addonKey, final String propertyKey)
    {
        return ao.get(AddOnPropertyAO.class, getPrimaryKeyForProperty(addonKey, propertyKey));
    }

    public enum PutResult
    {
        PROPERTY_CREATED,
        PROPERTY_UPDATED,
        PROPERTY_LIMIT_EXCEEDED
    }
}

