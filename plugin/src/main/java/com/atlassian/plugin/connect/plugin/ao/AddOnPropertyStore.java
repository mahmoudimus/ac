package com.atlassian.plugin.connect.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.fugue.Either;
import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.rest.data.ETag;
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

    public Either<GetResult, AddOnProperty> getPropertyValue(@Nonnull final String addOnKey, @Nonnull final String propertyKey, final Option<ETag> eTag)
    {
        AddOnPropertyAO[] properties = ao.find(AddOnPropertyAO.class, Query.select().where("PLUGIN_KEY = ? AND PROPERTY_KEY = ?", addOnKey, propertyKey));

        Option<AddOnPropertyAO> option = Iterables.first(Arrays.asList(properties));

        Option<AddOnProperty> propertyOption = option.map(new Function<AddOnPropertyAO, AddOnProperty>()
        {
            @Override
            public AddOnProperty apply(final AddOnPropertyAO input)
            {
                return new AddOnProperty(input.getPropertyKey(), input.getValue());
            }
        });

        if (!propertyOption.isDefined())
        {
            return Either.left(GetResult.PROPERTY_NOT_FOUND);
        }
        AddOnProperty property = propertyOption.get();
        if (eTag.isDefined() && eTag.get().equals(property.getETag()))
        {
            return Either.left(GetResult.PROPERTY_NOT_MODIFIED);
        }
        return Either.right(property);
    }

    public PutResult setPropertyValue(@Nonnull final String addOnKey, @Nonnull final String propertyKey, @Nonnull final String value, final Option<ETag> eTag)
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
                    ETag oldETag = AddOnProperty.fromAO(propertyAO).getETag();
                    if (eTag.isDefined() && !eTag.get().equals(oldETag))
                    {
                        return PutResult.PROPERTY_MODIFIED;
                    }
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

    public DeleteResult deletePropertyValue(@Nonnull final String addOnKey, @Nonnull final String propertyKey, @Nonnull final Option<ETag> eTag)
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
                    ETag oldETag = AddOnProperty.fromAO(propertyAO).getETag();
                    if (eTag.isDefined() && !eTag.get().equals(oldETag))
                    {
                        return DeleteResult.PROPERTY_MODIFIED;
                    }

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
    
    public Either<ListResult,AddOnPropertyIterable> getAllPropertiesForAddOnKey(@Nonnull final String addOnKey, final Option<ETag> eTag)
    {
        return ao.executeInTransaction(new TransactionCallback<Either<ListResult, AddOnPropertyIterable>>()
        {
            @Override
            public Either<ListResult, AddOnPropertyIterable> doInTransaction()
            {
                ImmutableList<AddOnPropertyAO> addOnPropertyAOList = ImmutableList.<AddOnPropertyAO>builder().add(getAddOnPropertyAOArrayForAddOnKey(addOnKey)).build();
                AddOnPropertyIterable addOnProperties = AddOnPropertyIterable.fromAddOnPropertyAOList(addOnPropertyAOList);

                if (eTag.isDefined() && eTag.get().equals(addOnProperties.getETag()))
                {
                    return Either.left(ListResult.PROPERTIES_NOT_MODIFIED);
                }
                return Either.right(addOnProperties);
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

    public enum GetResult
    {
        PROPERTY_NOT_FOUND,
        PROPERTY_NOT_MODIFIED
    }

    public enum PutResult
    {
        PROPERTY_CREATED,
        PROPERTY_UPDATED,
        PROPERTY_MODIFIED,
        PROPERTY_LIMIT_EXCEEDED
    }
    
    public enum DeleteResult
    {
        PROPERTY_DELETED,
        PROPERTY_MODIFIED,
        PROPERTY_NOT_FOUND
    }
    
    public enum ListResult
    {
        PROPERTIES_NOT_MODIFIED
    }
}

