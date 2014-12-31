package com.atlassian.plugin.connect.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
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
    public static final int MAX_PROPERTIES_PER_ADD_ON = 100;

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
                return new AddOnProperty(input.getPropertyKey(), input.getValue());
            }
        });
    }

    public PutResult setPropertyValue(@Nonnull final String addOnKey, @Nonnull final String propertyKey, @Nonnull final String value)
    {
        checkNotNull(addOnKey);
        checkNotNull(propertyKey);
        checkNotNull(value);
        return ao.executeInTransaction(new TransactionCallback<PutResult>()
        {
            @Override
            public PutResult doInTransaction()
            {
                if (hasReachedPropertyLimit(addOnKey))
                {
                    return PutResult.PROPERTY_LIMIT_EXCEEDED;
                }
                if (existsProperty(addOnKey, propertyKey))
                {
                    AddOnPropertyAO propertyAO = getAddOnPropertyForKey(addOnKey, propertyKey);
                    propertyAO.setValue(value);
                    propertyAO.save();
                    return PutResult.PROPERTY_UPDATED;
                }
                else
                {
                    AddOnPropertyAO property = ao.create(AddOnPropertyAO.class,
                            new DBParam("PLUGIN_KEY", addOnKey),
                            new DBParam("PROPERTY_KEY", propertyKey),
                            new DBParam("VALUE", value),
                            new DBParam("PRIMARY_KEY", getPrimaryKeyForProperty(addOnKey, propertyKey)));
                    property.save();
                    return PutResult.PROPERTY_CREATED;
                }
            }
        });
    }

    public DeleteResult deletePropertyValue(@Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        checkNotNull(addOnKey);
        checkNotNull(propertyKey);

        return ao.executeInTransaction(new TransactionCallback<DeleteResult>()
        {
            @Override
            public DeleteResult doInTransaction()
            {
                if (existsProperty(addOnKey, propertyKey))
                {
                    AddOnPropertyAO propertyAO = getAddOnPropertyForKey(addOnKey, propertyKey);
                    ao.delete(propertyAO);
                    return DeleteResult.PROPERTY_DELETED;
                }
                else
                {
                    return DeleteResult.PROPERTY_NOT_FOUND;
                }
            }
        });
    }

    private boolean existsProperty(@Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        return getAddOnPropertyForKey(addOnKey, propertyKey) != null;
    }
    
    public List<String> getAllPropertyKeysForAddOnKey(@Nonnull final String addOnKey)
    {
        ImmutableList<AddOnPropertyAO> addOnPropertyList = ImmutableList.<AddOnPropertyAO>builder().add(getAddOnPropertyAOArrayForAddOnKey(addOnKey)).build();
        return Lists.transform(addOnPropertyList, new Function<AddOnPropertyAO, String>()
        {
            @Override
            public String apply(final AddOnPropertyAO input)
            {
                return input.getPropertyKey();
            }
        });
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

    public enum PutResult
    {
        PROPERTY_CREATED,
        PROPERTY_UPDATED,
        PROPERTY_LIMIT_EXCEEDED
    }

    public enum DeleteResult
    {
        PROPERTY_DELETED,
        PROPERTY_NOT_FOUND
    }
}

