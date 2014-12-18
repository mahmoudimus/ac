package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyAO;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.google.common.base.Supplier;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getFirst;

/**
 * @since TODO: fill in the proper version before merge
 */
@Component
public class AddOnPropertyServiceImpl implements AddOnPropertyService
{
    public static final int MAXIMUM_VALUE_LENGTH = 32*1024; //32KB

    private final AddOnPropertyStore store;
    private final ConnectAddonManager connectAddonManager;
    private static final Gson gson = new Gson();


    @Autowired
    public AddOnPropertyServiceImpl(AddOnPropertyStore store, ConnectAddonManager connectAddonManager)
    {
        this.connectAddonManager = checkNotNull(connectAddonManager);
        this.store = checkNotNull(store);
    }

    private ValidationResult<GetPropertyInput> validateGetPropertyValue(final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        List<ServiceResultWithReason> errorCollection = new ArrayList<ServiceResultWithReason>();

        if (!hasPermissions(sourceAddOnKey,addOnKey))
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.ACCESS_FORBIDDEN, "Add-on does not have permission to access " + addOnKey + "'s data."));
            return ValidationResult.fromError(errorCollection);
        }
        if (!existsAddOn(addOnKey)) //a request cannot be made from an addon that does not exist, but we check just in case
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.ADD_ON_NOT_FOUND, "Add-on with key " + propertyKey + " not found."));
            return ValidationResult.fromError(errorCollection);
        }
        return ValidationResult.fromValue(new GetPropertyInput(addOnKey, propertyKey));
    }

    @Override
    public Either<ServiceResultWithReason, AddOnProperty> getPropertyValue(final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        ValidationResult<GetPropertyInput> validationResult = validateGetPropertyValue(sourceAddOnKey, addOnKey, propertyKey);
        checkNotNull(validationResult);
        if (!validationResult.isValid())
        {
            return Either.left(getFirst(validationResult.getErrorCollection(), null));
        }
        else
        {
            Option<AddOnProperty> propertyValue = store.getPropertyValue(addOnKey, propertyKey);
            return propertyValue.toRight(getSupplierForServiceResultWithReason(new ServiceResultWithReason(ServiceResult.PROPERTY_NOT_FOUND, "Property with key not found.")));
        }
    }

    private Supplier<ServiceResultWithReason> getSupplierForServiceResultWithReason(final ServiceResultWithReason resultWithReason)
    {
        return new Supplier<ServiceResultWithReason>() {

            @Override
            public ServiceResultWithReason get()
            {
                return resultWithReason;
            }
        };
    }

    private ValidationResult<SetPropertyInput> validateSetPropertyValue(final String sourceAddOnKey, @Nonnull final String addOnKey, final String propertyKey, final String value)
    {
        List<ServiceResultWithReason> errorCollection = new ArrayList<ServiceResultWithReason>();
        if (!hasPermissions(sourceAddOnKey,addOnKey))
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.ACCESS_FORBIDDEN, "Add-on does not have permission to access " + addOnKey + "'s data."));
            return ValidationResult.fromError(errorCollection);
        }
        if (!existsAddOn(addOnKey))
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.ADD_ON_NOT_FOUND, "Add-on with key " + propertyKey + " not found."));
            return ValidationResult.fromError(errorCollection);
        }

        if (propertyKey.length() >= AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH)
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.KEY_TOO_LONG, "The property key cannot be longer than 256 bytes."));
            return ValidationResult.fromError(errorCollection);
        }

        if (value.length() >= MAXIMUM_VALUE_LENGTH)
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.VALUE_TOO_BIG, "The value cannot be bigger than 32 KB."));
            return ValidationResult.fromError(errorCollection);
        }

        if (!isJSONValid(value))
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.INVALID_FORMAT, "The value has to be a valid json."));
            return ValidationResult.fromError(errorCollection);
        }

        return ValidationResult.fromValue(new SetPropertyInput(addOnKey, propertyKey, value));
    }

    @Override
    public ServiceResult setPropertyValue(final String sourceAddOnKey, @Nonnull final String addOnKey, final String propertyKey, final String value)
    {
        ValidationResult<SetPropertyInput> validationResult = validateSetPropertyValue(sourceAddOnKey, addOnKey, propertyKey, value);
        if (validationResult.isValid())
        {
            SetPropertyInput input = validationResult.getValue().getOrNull();
            AddOnPropertyStore.PutResult putResult = store.setPropertyValue(input.addOnKey, input.propertyKey, input.value);
            switch (putResult)
            {
                case PROPERTY_CREATED: return ServiceResult.PROPERTY_CREATED;
                case PROPERTY_UPDATED: return ServiceResult.PROPERTY_UPDATED;
                case PROPERTY_LIMIT_EXCEEDED: return ServiceResult.MAXIMUM_PROPERTIES_EXCEEDED;
            }
        }
        else
        {
            return validationResult.getErrorCollection().iterator().next().getResult();
        }

        return null;
    }

    private boolean existsAddOn(final String addOnKey)
    {
        return connectAddonManager.getExistingAddon(addOnKey) != null;
    }

    private boolean hasPermissions(String requestKey, String addOnKey)
    {
        return requestKey != null && requestKey.equals(addOnKey);
    }

    private class GetPropertyInput
    {
        final String addOnKey;
        final String propertyKey;

        public GetPropertyInput(final String addOnKey, final String propertyKey)
        {
            this.addOnKey = addOnKey;
            this.propertyKey = propertyKey;
        }

        public String getAddOnKey()
        {
            return addOnKey;
        }

        public String getPropertyKey()
        {
            return propertyKey;
        }
    }
    private class SetPropertyInput
    {
        final String addOnKey;
        final String propertyKey;
        final String value;

        public SetPropertyInput(final String addOnKey, final String propertyKey, final String value)
        {
            this.addOnKey = addOnKey;
            this.propertyKey = propertyKey;
            this.value = value;
        }

        public String getAddOnKey()
        {
            return addOnKey;
        }

        public String getPropertyKey()
        {
            return propertyKey;
        }

        public String getValue()
        {
            return value;
        }
    }

    private boolean isJSONValid(String jsonString) {
        try
        {
            gson.fromJson(jsonString, Object.class);
            return true;
        }
        catch(com.google.gson.JsonSyntaxException ex)
        {
            return false;
        }
    }
}
