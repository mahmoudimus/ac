package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.fugue.Suppliers;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyAO;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AddOnPropertyServiceImpl.class);

    public enum ServiceResultImpl implements ServiceResult
    {
        PROPERTY_UPDATED(HttpStatus.SC_OK, "Property updated."),
        PROPERTY_CREATED(HttpStatus.SC_CREATED, "Property created."),
        KEY_TOO_LONG(HttpStatus.SC_BAD_REQUEST, String.format("The property key cannot be longer than %s bytes.", AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH)),
        MAXIMUM_PROPERTIES_EXCEEDED(HttpStatus.SC_CONFLICT, String.format("Maximum number of properties allowed to be stored (%s) has been reached.", AddOnPropertyStore.MAX_PROPERTIES_PER_ADD_ON)),
        PROPERTY_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "Property with key not found."),
        ACCESS_TO_OTHER_DATA_FORBIDDEN(HttpStatus.SC_FORBIDDEN, "An add-on may only access it's own data"),
        INVALID_PROPERTY_VALUE(HttpStatus.SC_BAD_REQUEST, "Property value must be a valid JSON object"),
        NOT_AUTHENTICATED(HttpStatus.SC_UNAUTHORIZED, "Access to this resource must be authenticated as an add-on"),
        PROPERTY_DELETED(HttpStatus.SC_NO_CONTENT, null);

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

    public static final int MAXIMUM_PROPERTY_VALUE_LENGTH = 32*1024; //32KB
    private static final Gson gson = new Gson();

    private final AddOnPropertyStore store;
    private final UserManager userManager;

    @Autowired
    public AddOnPropertyServiceImpl(AddOnPropertyStore store, UserManager userManager)
    {
        this.store = checkNotNull(store);
        this.userManager = checkNotNull(userManager);
    }

    private ValidationResult<GetOrDeletePropertyInput> validateGetPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        if (!loggedIn(user))
        {
            return ValidationResult.fromError(ServiceResultImpl.NOT_AUTHENTICATED);
        }
        if (!pluginHasPermissions(sourceAddOnKey, addOnKey) && !isSysAdmin(user))
        {
            return ValidationResult.fromError(ServiceResultImpl.ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        if (propertyKey.length() > AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH)
        {
            return ValidationResult.fromError(ServiceResultImpl.KEY_TOO_LONG);
        }
        return ValidationResult.fromValue(new GetOrDeletePropertyInput(addOnKey, propertyKey));
    }

    private boolean isSysAdmin(final UserProfile user)
    {
        return userManager.isSystemAdmin(user.getUserKey());
    }

    private boolean loggedIn(final UserProfile user)
    {
        return user != null;
    }

    @Override
    public Either<ServiceResult, AddOnProperty> getPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        ValidationResult<GetOrDeletePropertyInput> validationResult = validateGetPropertyValue(user, sourceAddOnKey, addOnKey, propertyKey);
        if (validationResult.isValid())
        {
            GetOrDeletePropertyInput input = validationResult.getValue().getOrNull();
            Option<AddOnProperty> propertyValue = store.getPropertyValue(input.getAddOnKey(), input.getPropertyKey());
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
            return ValidationResult.fromError(ServiceResultImpl.NOT_AUTHENTICATED);
        }
        if (!pluginHasPermissions(sourceAddOnKey, addOnKey) && !isSysAdmin(user))
        {
            return ValidationResult.fromError(ServiceResultImpl.ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        if (propertyKey.length() > AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH)
        {
            return ValidationResult.fromError(ServiceResultImpl.KEY_TOO_LONG);
        }
        if (!isJSONValid(value))
        {
            return ValidationResult.fromError(ServiceResultImpl.INVALID_PROPERTY_VALUE);
        }
        return ValidationResult.fromValue(new SetPropertyInput(addOnKey, propertyKey, value));
    }

    @Override
    public ServiceResult setPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey, @Nonnull final String value)
    {
        Preconditions.checkArgument(value.length() <= MAXIMUM_PROPERTY_VALUE_LENGTH);
        ValidationResult<SetPropertyInput> validationResult = validateSetPropertyValue(user, sourceAddOnKey, checkNotNull(addOnKey), checkNotNull(propertyKey), checkNotNull(value));
        if (validationResult.isValid())
        {
            SetPropertyInput input = validationResult.getValue().getOrNull();
            AddOnPropertyStore.PutResult putResult = store.setPropertyValue(input.getAddOnKey(), input.getPropertyKey(), input.getValue());
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

    
    private ValidationResult<GetOrDeletePropertyInput> validateDeletePropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        if (!loggedIn(user))
        {
            return ValidationResult.fromError(ServiceResultImpl.NOT_AUTHENTICATED);
        }
        if (!pluginHasPermissions(sourceAddOnKey,addOnKey) && !isSysAdmin(user))
        {
            return ValidationResult.fromError(ServiceResultImpl.ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        if (propertyKey.length() > AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH)
        {
            return ValidationResult.fromError(ServiceResultImpl.KEY_TOO_LONG);
        }
        return ValidationResult.fromValue(new GetOrDeletePropertyInput(addOnKey, propertyKey));
    }

    @Override
    public ServiceResult deletePropertyValue(@Nullable final UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        ValidationResult<GetOrDeletePropertyInput> validationResult = validateDeletePropertyValue(user, sourceAddOnKey, checkNotNull(addOnKey), checkNotNull(propertyKey));
        if (validationResult.isValid())
        {
            GetOrDeletePropertyInput input = validationResult.getValue().getOrNull();
            AddOnPropertyStore.DeleteResult deleteResult = store.deletePropertyValue(input.addOnKey, input.propertyKey);
            switch (deleteResult)
            {
                case PROPERTY_DELETED: return ServiceResultImpl.PROPERTY_DELETED;
                case PROPERTY_NOT_FOUND: return ServiceResultImpl.PROPERTY_NOT_FOUND;
                default: throw new IllegalStateException();
            }
        }
        else
        {
            return validationResult.getError().getOrNull();
        }
    }

    private boolean pluginHasPermissions(String requestKey, String addOnKey)
    {
        return requestKey != null && requestKey.equals(addOnKey);
    }

    private class GetOrDeletePropertyInput
    {
        final String addOnKey;
        final String propertyKey;

        public GetOrDeletePropertyInput(final String addOnKey, final String propertyKey)
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
        final GetOrDeletePropertyInput input;
        final String value;

        public SetPropertyInput(final String addOnKey, final String propertyKey, final String value)
        {
            this.input = new GetOrDeletePropertyInput(addOnKey,propertyKey);
            this.value = value;
        }

        public String getAddOnKey()
        {
            return input.getAddOnKey();
        }

        public String getPropertyKey()
        {
            return input.getPropertyKey();
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
            log.debug("Invalid json when setting property value for plugin.");
            return false;
        }
    }
}
