package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.fugue.Suppliers;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyAO;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.apache.commons.httpclient.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since TODO: fill in the proper version before merge
 */
@Component
public class AddOnPropertyServiceImpl implements AddOnPropertyService
{

    public enum ServiceResultImpl implements ServiceResult
    {
        PROPERTY_UPDATED(HttpStatus.SC_OK, "Property updated."),
        PROPERTY_CREATED(HttpStatus.SC_CREATED, "Property created."),
        KEY_TOO_LONG(HttpStatus.SC_BAD_REQUEST, "The property key cannot be longer than 256 bytes."),
        MAXIMUM_PROPERTIES_EXCEEDED(HttpStatus.SC_CONFLICT, "Add-on has reached maximum properties."),
        PROPERTY_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "Property with key not found."),
        ACCESS_FORBIDDEN(HttpStatus.SC_FORBIDDEN, "Add-on does not have permission to access other plugins data."),
        INVALID_FORMAT(HttpStatus.SC_BAD_REQUEST, "The value has to be a valid json."),
        NOT_LOGGED_IN(HttpStatus.SC_UNAUTHORIZED, "You have to be logged in.");

        private final int httpStatusCode;
        private final String message;

        private ServiceResultImpl(int httpStatusCode, String message)
        {
            this.httpStatusCode = httpStatusCode;
            this.message = message;
        }

        public int getHttpStatusCode()
        {
            return httpStatusCode;
        }

        public String message()
        {
            return message;
        }
    }

    public static final int MAXIMUM_VALUE_LENGTH = 32*1024; //32KB
    private static final Gson gson = new Gson();

    private final AddOnPropertyStore store;

    @Autowired
    public AddOnPropertyServiceImpl(AddOnPropertyStore store)
    {
        this.store = checkNotNull(store);
    }

    private ValidationResult<GetPropertyInput> validateGetPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        if (!loggedIn(user))
        {
            return ValidationResult.fromError(ServiceResultImpl.NOT_LOGGED_IN);
        }
        if (!hasPermissions(sourceAddOnKey,addOnKey))
        {
            return ValidationResult.fromError(ServiceResultImpl.ACCESS_FORBIDDEN);
        }
        return ValidationResult.fromValue(new GetPropertyInput(addOnKey, propertyKey));
    }

    private boolean loggedIn(final UserProfile user)
    {
        return user != null;
    }

    @Override
    public Either<ServiceResult, AddOnProperty> getPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        ValidationResult<GetPropertyInput> validationResult = validateGetPropertyValue(user, sourceAddOnKey, addOnKey, propertyKey);
        if (validationResult.isValid())
        {
            Option<AddOnProperty> propertyValue = store.getPropertyValue(addOnKey, propertyKey);
            return propertyValue.toRight(Suppliers.ofInstance((ServiceResult)ServiceResultImpl.PROPERTY_NOT_FOUND));
        }
        else
        {
            return Either.left(validationResult.getError().getOrNull());
        }
    }

    private ValidationResult<SetPropertyInput> validateSetPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, final String propertyKey, final String value)
    {
        if (!loggedIn(user))
        {
            return ValidationResult.fromError(ServiceResultImpl.NOT_LOGGED_IN);
        }
        if (!hasPermissions(sourceAddOnKey,addOnKey))
        {
            return ValidationResult.fromError(ServiceResultImpl.ACCESS_FORBIDDEN);
        }
        if (propertyKey.length() >= AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH)
        {
            return ValidationResult.fromError(ServiceResultImpl.KEY_TOO_LONG);
        }
        if (!isJSONValid(value))
        {
            return ValidationResult.fromError(ServiceResultImpl.INVALID_FORMAT);
        }
        return ValidationResult.fromValue(new SetPropertyInput(addOnKey, propertyKey, value));
    }

    @Override
    public ServiceResult setPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey, @Nonnull final String value)
    {
        Preconditions.checkArgument(value.length() <= MAXIMUM_VALUE_LENGTH);
        ValidationResult<SetPropertyInput> validationResult = validateSetPropertyValue(user, sourceAddOnKey, checkNotNull(addOnKey), checkNotNull(propertyKey), checkNotNull(value));
        if (validationResult.isValid())
        {
            SetPropertyInput input = validationResult.getValue().getOrNull();
            AddOnPropertyStore.PutResult putResult = store.setPropertyValue(input.addOnKey, input.propertyKey, input.value);
            switch (putResult)
            {
                case PROPERTY_CREATED: return ServiceResultImpl.PROPERTY_CREATED;
                case PROPERTY_UPDATED: return ServiceResultImpl.PROPERTY_UPDATED;
                case PROPERTY_LIMIT_EXCEEDED: return ServiceResultImpl.MAXIMUM_PROPERTIES_EXCEEDED;
                default: throw new IllegalStateException();
            }
        }
        else
        {
            return validationResult.getError().getOrNull();
        }
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
