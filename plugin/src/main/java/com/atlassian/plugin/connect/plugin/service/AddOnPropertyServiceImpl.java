package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.fugue.Either;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getFirst;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
@Component
public class AddOnPropertyServiceImpl implements AddOnPropertyService
{
    public static final int MAXIMUM_KEY_LENGTH = 255;
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

    private ValidationResult<GetPropertyInput> validateGetPropertyValue(final String sourceAddonKey, @Nonnull final String addonKey, @Nonnull final String propertyKey)
    {
        List<ServiceResultWithReason> errorCollection = new ArrayList<ServiceResultWithReason>();

        if (!hasPermissions(sourceAddonKey,addonKey))
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.ACCESS_FORBIDDEN, "Add-on does not have permission to access " + addonKey + "'s data."));
            return new ValidationResult<GetPropertyInput>(Either.<List<ServiceResultWithReason>,GetPropertyInput>left(errorCollection));
        }
        if (!existsAddon(addonKey)) //a request cannot be made from an addon that does not exist, but we check just in case
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.ADDON_NOT_FOUND, "Addon with key " + propertyKey + " not found."));
            return new ValidationResult<GetPropertyInput>(Either.<List<ServiceResultWithReason>,GetPropertyInput>left(errorCollection));
        }
        return new ValidationResult<GetPropertyInput>(Either.<List<ServiceResultWithReason>,GetPropertyInput>right(new GetPropertyInput(addonKey, propertyKey)));
    }

    @Override
    public Either<ServiceResultWithReason, AddOnProperty> getPropertyValue(final String sourceAddonKey, @Nonnull final String addonKey, @Nonnull final String propertyKey)
    {
        ValidationResult<GetPropertyInput> validationResult = validateGetPropertyValue(sourceAddonKey, addonKey, propertyKey);
        checkNotNull(validationResult);
        if (!validationResult.isValid())
        {
            return Either.left(getFirst(validationResult.getErrorCollection(), null));
        }
        else
        {
            Optional<AddOnProperty> propertyValue = store.getPropertyValue(addonKey, propertyKey);
            if (propertyValue.isPresent())
            {
                return Either.right(propertyValue.get());
            }
            else
            {
                return Either.left(new ServiceResultWithReason(ServiceResult.PROPERTY_NOT_FOUND, "Property with key not found."));
            }
        }
    }

    private ValidationResult<SetPropertyInput> validateSetPropertyValue(final String sourceAddonKey, @Nonnull final String addonKey, final String propertyKey, final String value)
    {
        List<ServiceResultWithReason> errorCollection = new ArrayList<ServiceResultWithReason>();
        if (!hasPermissions(sourceAddonKey,addonKey))
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.ACCESS_FORBIDDEN, "Add-on does not have permission to access " + addonKey + "'s data."));
            return new ValidationResult<SetPropertyInput>(Either.<List<ServiceResultWithReason>,SetPropertyInput>left(errorCollection));
        }
        if (!existsAddon(addonKey))
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.ADDON_NOT_FOUND, "Addon with key " + propertyKey + " not found."));
            return new ValidationResult<SetPropertyInput>(Either.<List<ServiceResultWithReason>,SetPropertyInput>left(errorCollection));
        }

        if (propertyKey.length() >= MAXIMUM_KEY_LENGTH)
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.KEY_TOO_LONG, "The property key cannot be longer than 255 bytes."));
            return new ValidationResult<SetPropertyInput>(Either.<List<ServiceResultWithReason>,SetPropertyInput>left(errorCollection));
        }

        if (value.length() >= MAXIMUM_VALUE_LENGTH)
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.VALUE_TOO_BIG, "The value cannot be bigger than 32 KB."));
            return new ValidationResult<SetPropertyInput>(Either.<List<ServiceResultWithReason>,SetPropertyInput>left(errorCollection));
        }

        if (!isJSONValid(value))
        {
            errorCollection.add(new ServiceResultWithReason(ServiceResult.INVALID_FORMAT, "The value has to be a valid json."));
            return new ValidationResult<SetPropertyInput>(Either.<List<ServiceResultWithReason>,SetPropertyInput>left(errorCollection));
        }
        return new ValidationResult<SetPropertyInput>(Either.<List<ServiceResultWithReason>,SetPropertyInput>right(new SetPropertyInput(addonKey, propertyKey, value)));
    }

    @Override
    public ServiceResult setPropertyValue(final String sourceAddonKey, @Nonnull final String addonKey, final String propertyKey, final String value)
    {
        ValidationResult<SetPropertyInput> validationResult = validateSetPropertyValue(sourceAddonKey, addonKey, propertyKey, value);
        if (validationResult.isValid())
        {
            SetPropertyInput input = validationResult.getValue().getOrNull();
            AddOnPropertyStore.PutResult putResult = store.setPropertyValue(input.addonKey, input.propertyKey, input.value);
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

    private boolean existsAddon(final String addonKey)
    {
        return connectAddonManager.getExistingAddon(addonKey) != null;
    }

    private boolean hasPermissions(String requestKey, String addonKey)
    {
        return requestKey != null && requestKey.equals(addonKey);
    }

    private class GetPropertyInput
    {
        final String addonKey;
        final String propertyKey;

        public GetPropertyInput(final String addonKey, final String propertyKey)
        {
            this.addonKey = addonKey;
            this.propertyKey = propertyKey;
        }

        public String getAddonKey()
        {
            return addonKey;
        }

        public String getPropertyKey()
        {
            return propertyKey;
        }
    }
    private class SetPropertyInput
    {
        final String addonKey;
        final String propertyKey;
        final String value;

        public SetPropertyInput(final String addonKey, final String propertyKey, final String value)
        {
            this.addonKey = addonKey;
            this.propertyKey = propertyKey;
            this.value = value;
        }

        public String getAddonKey()
        {
            return addonKey;
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
