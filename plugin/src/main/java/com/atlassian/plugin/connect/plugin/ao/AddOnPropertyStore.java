package com.atlassian.plugin.connect.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.util.ConfigurationUtils;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class allows for persisting add-on properties.
 * @see com.atlassian.plugin.connect.plugin.service.AddOnPropertyServiceImpl
 *
 * @since TODO: fill in the proper version before merge
 */
@Component
public class AddOnPropertyStore
{
    private final ActiveObjects ao;

    private static final String MAX_PROPERTIES_SYSTEM_PROPERTY = "com.atlassian.plugin.connect.add_on_properties.max_properties";
    private static final int MAX_PROPERTIES_DEFAULT = 100;
    public static final int MAX_PROPERTIES_PER_ADD_ON = ConfigurationUtils.getIntSystemProperty(MAX_PROPERTIES_SYSTEM_PROPERTY, MAX_PROPERTIES_DEFAULT);

    @Autowired
    public AddOnPropertyStore(final ActiveObjects ao) {this.ao = checkNotNull(ao);}

    public Option<AddOnProperty> getPropertyValue(@Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        AddOnPropertyAO[] properties = ao.find(AddOnPropertyAO.class, Query.select().where("PLUGIN_KEY = ? AND PROPERTY_KEY = ?", addOnKey, propertyKey));

        Option<AddOnPropertyAO> option = Iterables.first(Arrays.asList(properties));

        return option.map(new Function<AddOnPropertyAO, AddOnProperty>()
        {
            @Override
            public AddOnProperty apply(final AddOnPropertyAO input)
            {
                return new AddOnProperty(input.getPropertyKey(), input.getValue(), input.getModificationTime());
            }
        });
    }

    public PutResultWithOptionalProperty setPropertyValue(@Nonnull final String addOnKey, @Nonnull final String propertyKey, @Nonnull final String value)
    {
        checkNotNull(addOnKey);
        checkNotNull(propertyKey);
        checkNotNull(value);
        if (hasReachedPropertyLimit(addOnKey))
        {
            return new PutResultWithOptionalProperty(PutResult.PROPERTY_LIMIT_EXCEEDED, Option.<AddOnProperty>none());
        }
        if (existsProperty(addOnKey, propertyKey))
        {
            AddOnPropertyAO propertyAO = getAddOnPropertyForKey(addOnKey, propertyKey);
            propertyAO.setValue(value);
            propertyAO.setModificationTime(System.nanoTime());
            propertyAO.save();
            return new PutResultWithOptionalProperty(PutResult.PROPERTY_UPDATED, Option.some(AddOnProperty.fromAO(propertyAO)));
        }
        else
        {
            AddOnPropertyAO property = ao.create(AddOnPropertyAO.class,
                    new DBParam("PLUGIN_KEY", addOnKey),
                    new DBParam("PROPERTY_KEY", propertyKey),
                    new DBParam("VALUE", value),
                    new DBParam("PRIMARY_KEY", getPrimaryKeyForProperty(addOnKey, propertyKey)),
                    new DBParam("MODIFICATION_TIME", System.nanoTime()));
            property.save();
            return new PutResultWithOptionalProperty(PutResult.PROPERTY_CREATED, Option.some(AddOnProperty.fromAO(property)));
        }
    }

    public void deletePropertyValue(@Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        checkNotNull(addOnKey);
        checkNotNull(propertyKey);

        AddOnPropertyAO propertyAO = getAddOnPropertyForKey(addOnKey, propertyKey);
        ao.delete(propertyAO);
    }

    public AddOnPropertyIterable getAllPropertiesForAddOnKey(@Nonnull final String addOnKey)
    {
        return ao.executeInTransaction(new TransactionCallback<AddOnPropertyIterable>()
        {
            @Override
            public AddOnPropertyIterable doInTransaction()
            {
                ImmutableList<AddOnPropertyAO> addOnPropertyAOList = ImmutableList.<AddOnPropertyAO>builder().add(getAddOnPropertyAOArrayForAddOnKey(addOnKey)).build();
                return AddOnPropertyIterable.fromAddOnPropertyAOList(addOnPropertyAOList);
            }
        });
    }

    public <T> T executeInTransaction(@Nonnull final TransactionCallable<T> function)
    {
        return ao.executeInTransaction(new TransactionCallback<T>()
        {
            @Override
            public T doInTransaction()
            {
                return function.call();
            }
        });
    }

    private boolean existsProperty(@Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        return getAddOnPropertyForKey(addOnKey, propertyKey) != null;
    }

    private AddOnPropertyAO[] getAddOnPropertyAOArrayForAddOnKey(final String addOnKey) {return ao.find(AddOnPropertyAO.class, Query.select().where("PLUGIN_KEY = ?", addOnKey));}

    private String getPrimaryKeyForProperty(@Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        return String.format("%s:%s", addOnKey, propertyKey);
    }

    private AddOnPropertyAO getAddOnPropertyForKey(@Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        return ao.get(AddOnPropertyAO.class, getPrimaryKeyForProperty(addOnKey, propertyKey));
    }

    private boolean hasReachedPropertyLimit(@Nonnull final String addOnKey)
    {
        return ao.count(AddOnPropertyAO.class, Query.select().where("PLUGIN_KEY = ?", addOnKey)) >= MAX_PROPERTIES_PER_ADD_ON;
    }

    public enum PutResult implements StoreResult
    {
        PROPERTY_CREATED,
        PROPERTY_UPDATED,
        PROPERTY_MODIFIED,
        PROPERTY_LIMIT_EXCEEDED
    }
    
    public enum DeleteResult implements StoreResult
    {
        PROPERTY_DELETED,
        PROPERTY_NOT_FOUND
    }

    public interface StoreResult {}

    public static class PutResultWithOptionalProperty
    {
        private final PutResult result;
        private final Option<AddOnProperty> property;

        public PutResultWithOptionalProperty(final PutResult result, final Option<AddOnProperty> property)
        {
            this.result = result;
            this.property = property;
        }

        public PutResult getResult()
        {
            return result;
        }

        public Option<AddOnProperty> getProperty()
        {
            return property;
        }
    }

    public interface TransactionCallable<T>
    {
        T call();
    }
}

