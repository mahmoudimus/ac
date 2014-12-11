package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.fugue.Either;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.google.common.base.Optional;
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
public class AddOnPropertyServiceImpl implements AddOnPropertyService
{
    public static final int MAXIMUM_KEY_LENGTH = 255;
    public static final int MAXIMUM_VALUE_LENGTH = 32*1024; //32KB

    private final AddOnPropertyStore store;
    private final ConnectAddonManager connectAddonManager;


    @Autowired
    public AddOnPropertyServiceImpl(AddOnPropertyStore store, ConnectAddonManager connectAddonManager)
    {
        this.connectAddonManager = checkNotNull(connectAddonManager);
        this.store = checkNotNull(store);
    }

    private ValidationResult<GetPropertyInput> validateGetPropertyValue(@Nonnull final String addonKey, @Nonnull final String propertyKey)
    {
        List<ValidationErrorWithReason> errorCollection = new ArrayList<ValidationErrorWithReason>();
        if (!existsAddon(addonKey))
        {
            errorCollection.add(new ValidationErrorWithReason(OperationStatus.ADDON_NOT_FOUND, "Addon with key " + propertyKey + " not found."));
            return new ValidationResult<GetPropertyInput>(Either.<GetPropertyInput,List<ValidationErrorWithReason>>right(errorCollection));
        }
        return new ValidationResult<GetPropertyInput>(Either.<GetPropertyInput,List<ValidationErrorWithReason>>left(new GetPropertyInput(addonKey, propertyKey)));
    }

    @Override
    public Either<AddOnProperty,Iterable<ValidationErrorWithReason>> getPropertyValue(@Nonnull final String addonKey, @Nonnull final String propertyKey)
    {
        ValidationResult<GetPropertyInput> validationResult = validateGetPropertyValue(addonKey, propertyKey);
        checkNotNull(validationResult);
        if (!validationResult.isValid())
        {
            return Either.right(validationResult.getErrorCollection());
        }
        else
        {
            Optional<AddOnProperty> propertyValue = store.getPropertyValue(addonKey, propertyKey);
            if (propertyValue.isPresent())
                return Either.left(propertyValue.get());
            else
            {
                Iterable<ValidationErrorWithReason> validationErrorWithReasons = Arrays.asList(new ValidationErrorWithReason(OperationStatus.PROPERTY_NOT_FOUND, "Property with key not found."));
                return Either.right(validationErrorWithReasons);
            }
        }
    }

    private ValidationResult<SetPropertyInput> validateSetPropertyValue(@Nonnull final String addonKey, final String propertyKey, final String value)
    {
        List<ValidationErrorWithReason> errorCollection = new ArrayList<ValidationErrorWithReason>();
        if (!existsAddon(addonKey))
        {
            errorCollection.add(new ValidationErrorWithReason(OperationStatus.ADDON_NOT_FOUND, "Addon with key " + propertyKey + " not found."));
            return new ValidationResult<SetPropertyInput>(Either.<SetPropertyInput,List<ValidationErrorWithReason>>right(errorCollection));
        }

        if (propertyKey.length() >= MAXIMUM_KEY_LENGTH)
        {
            errorCollection.add(new ValidationErrorWithReason(OperationStatus.KEY_TOO_LONG, "The property key cannot be longer than 255 bytes."));
            return new ValidationResult<SetPropertyInput>(Either.<SetPropertyInput,List<ValidationErrorWithReason>>right(errorCollection));
        }

        if (value.length() >= MAXIMUM_VALUE_LENGTH)
        {
            errorCollection.add(new ValidationErrorWithReason(OperationStatus.VALUE_TOO_BIG, "The value cannot be bigger than 32 KB."));
            return new ValidationResult<SetPropertyInput>(Either.<SetPropertyInput,List<ValidationErrorWithReason>>right(errorCollection));
        }
        return new ValidationResult<SetPropertyInput>(Either.<SetPropertyInput,List<ValidationErrorWithReason>>left(new SetPropertyInput(addonKey, propertyKey, value)));
    }

    @Override
    public OperationStatus setPropertyValue(@Nonnull final String addonKey, final String propertyKey, final String value)
    {
        ValidationResult<SetPropertyInput> validationResult = validateSetPropertyValue(addonKey, propertyKey, value);
        if (validationResult.isValid())
        {
            SetPropertyInput input = validationResult.getValue().getOrNull();
            AddOnPropertyStore.PutResult putResult = store.setPropertyValue(input.addonKey, input.propertyKey, input.value);
            switch (putResult)
            {
                case PROPERTY_CREATED: return OperationStatus.PROPERTY_CREATED;
                case PROPERTY_UPDATED: return OperationStatus.PROPERTY_UPDATED;
                case PROPERTY_LIMIT_EXCEEDED: return OperationStatus.MAXIMUM_PROPERTIES_EXCEEDED;
            }
        }
        else
        {
            return validationResult.getErrorCollection().iterator().next().getError();
        }

        return null;
    }

    private boolean existsAddon(final String addonKey)
    {
        return connectAddonManager.getExistingAddon(addonKey) != null;
    }

    private boolean hasPermissions(String requestKey, String addonKey)
    {
        return requestKey.equals(addonKey);
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
}
