package com.atlassian.plugin.connect.plugin.property;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.java.ao.DBParam;
import net.java.ao.Query;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class allows for persisting add-on properties.
 * @see com.atlassian.plugin.connect.plugin.property.AddOnPropertyServiceImpl
 */
@Component
public class AddOnPropertyStore
{
    private final ActiveObjects ao;

    private static final String MAX_PROPERTIES_SYSTEM_PROPERTY = "com.atlassian.plugin.connect.add_on_properties.max_properties";
    private static final int MAX_PROPERTIES_DEFAULT = 100;
    public static final int MAX_PROPERTIES_PER_ADD_ON = Integer.getInteger(MAX_PROPERTIES_SYSTEM_PROPERTY,
        MAX_PROPERTIES_DEFAULT);

    @Autowired
    public AddOnPropertyStore(final ActiveObjects ao) {this.ao = checkNotNull(ao);}

    public Optional<AddOnProperty> getPropertyValue(@Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        AddOnPropertyAO[] properties = ao.find(AddOnPropertyAO.class, Query.select().where("PLUGIN_KEY = ? AND PROPERTY_KEY = ?", addOnKey, propertyKey));

        Optional<AddOnPropertyAO> option = Optional.ofNullable(Iterables.getFirst(Arrays.asList(properties), null));

        return option.flatMap(new Function<AddOnPropertyAO, Optional<AddOnProperty>>()
        {
            @Override
            public Optional<AddOnProperty> apply(AddOnPropertyAO addOnPropertyAO)
            {
                return Optional.of(AddOnProperty.fromAO(addOnPropertyAO));
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
            return new PutResultWithOptionalProperty(PutResult.PROPERTY_LIMIT_EXCEEDED, Optional.<AddOnProperty>empty());
        }
        if (existsProperty(addOnKey, propertyKey))
        {
            AddOnPropertyAO previousPropertyAO = getAddOnPropertyForKey(addOnKey, propertyKey);
            ao.delete(previousPropertyAO); //delete and create to get a new auto-incremented version for property

            AddOnPropertyAO newPropertyAO = createAddOnProperty(addOnKey, propertyKey, value);
            newPropertyAO.save();
            return new PutResultWithOptionalProperty(PutResult.PROPERTY_UPDATED, Optional.of(AddOnProperty.fromAO(
                newPropertyAO)));
        }
        else
        {
            AddOnPropertyAO property = createAddOnProperty(addOnKey, propertyKey, value);
            property.save();
            return new PutResultWithOptionalProperty(PutResult.PROPERTY_CREATED, Optional.of(AddOnProperty.fromAO(
                property)));
        }
    }

    private AddOnPropertyAO createAddOnProperty(final String addOnKey, final String propertyKey, final String value)
    {
        return ao.create(AddOnPropertyAO.class,
                new DBParam("PLUGIN_KEY", addOnKey),
                new DBParam("PROPERTY_KEY", propertyKey),
                new DBParam("VALUE", value),
                new DBParam("PRIMARY_KEY", getPrimaryKeyForProperty(addOnKey, propertyKey)));
    }

    public void deletePropertyValue(@Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        checkNotNull(addOnKey);
        checkNotNull(propertyKey);

        AddOnPropertyAO propertyAO = getAddOnPropertyForKey(addOnKey, propertyKey);
        if (propertyAO != null)
        {
            ao.delete(propertyAO);
        }
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

    public <T> T executeInTransaction(@Nonnull final TransactionAction<T> function)
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
        AddOnPropertyAO[] addOnPropertyAOs = ao.find(AddOnPropertyAO.class, Query.select().where("PRIMARY_KEY = ?", getPrimaryKeyForProperty(addOnKey, propertyKey)));
        return Iterables.getFirst(Arrays.asList(addOnPropertyAOs), null);
    }

    private boolean hasReachedPropertyLimit(@Nonnull final String addOnKey)
    {
        return ao.count(AddOnPropertyAO.class, Query.select().where("PLUGIN_KEY = ?", addOnKey)) >= MAX_PROPERTIES_PER_ADD_ON;
    }

    public enum PutResult
    {
        PROPERTY_CREATED,
        PROPERTY_UPDATED,
        PROPERTY_LIMIT_EXCEEDED
    }

    public static class PutResultWithOptionalProperty
    {
        private final PutResult result;
        private final Optional<AddOnProperty> property;

        public PutResultWithOptionalProperty(final PutResult result, final Optional<AddOnProperty> property)
        {
            this.result = result;
            this.property = property;
        }

        public PutResult getResult()
        {
            return result;
        }

        public Optional<AddOnProperty> getProperty()
        {
            return property;
        }
    }

    public interface TransactionAction<T>
    {
        T call();
    }
}

