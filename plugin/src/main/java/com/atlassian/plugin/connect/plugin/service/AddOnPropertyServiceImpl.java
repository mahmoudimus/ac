package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyAO;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyIterable;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.rest.data.ETag;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Function;
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
        KEY_TOO_LONG(HttpStatus.SC_BAD_REQUEST, String.format("The property key cannot be longer than %s characters.", AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH)),
        MAXIMUM_PROPERTIES_EXCEEDED(HttpStatus.SC_CONFLICT, String.format("Maximum number of properties allowed to be stored (%s) has been reached.", AddOnPropertyStore.MAX_PROPERTIES_PER_ADD_ON)),
        PROPERTY_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "Property with key not found."),
        INVALID_PROPERTY_VALUE(HttpStatus.SC_BAD_REQUEST, "Property value must be a valid JSON object"),
        NOT_AUTHENTICATED(HttpStatus.SC_UNAUTHORIZED, "Access to this resource must be authenticated as an add-on"),
        PROPERTY_DELETED(HttpStatus.SC_NO_CONTENT, null),
        ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN(HttpStatus.SC_NOT_FOUND, "Add-on with key does not exist."),
        PROPERTY_NOT_MODIFIED(HttpStatus.SC_NOT_MODIFIED, "Not modified"),
        PROPERTY_MODIFIED(HttpStatus.SC_PRECONDITION_FAILED, "This resource has been updated."),
        PROPERTIES_NOT_MODIFIED(HttpStatus.SC_NOT_MODIFIED, "No properties have been updated.");

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
    private final ConnectAddonRegistry connectAddonRegistry;

    @Autowired
    public AddOnPropertyServiceImpl(AddOnPropertyStore store, UserManager userManager, ConnectAddonRegistry connectAddonRegistry)
    {
        this.store = checkNotNull(store);
        this.userManager = checkNotNull(userManager);
        this.connectAddonRegistry = checkNotNull(connectAddonRegistry);
    }

    @Override
    public Either<ServiceResult, AddOnProperty> getPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey, final Option<ETag> eTag)
    {
        ValidationResult<GetOrDeletePropertyInput> validationResult = validateGetPropertyValue(user, sourceAddOnKey, addOnKey, propertyKey);
        if (validationResult.isValid())
        {
            GetOrDeletePropertyInput input = validationResult.getValue().getOrNull();
            Either<AddOnPropertyStore.GetResult, AddOnProperty> propertyValue = store.getPropertyValue(input.getAddOnKey(), input.getPropertyKey(), eTag);
            return propertyValue.left().map(new Function<AddOnPropertyStore.GetResult, ServiceResult>()
            {
                @Override
                public ServiceResult apply(final AddOnPropertyStore.GetResult getResult)
                {
                    switch (getResult)
                    {
                        case PROPERTY_NOT_FOUND:
                            return ServiceResultImpl.PROPERTY_NOT_FOUND;
                        case PROPERTY_NOT_MODIFIED:
                            return ServiceResultImpl.PROPERTY_NOT_MODIFIED;
                        default:
                            throw new IllegalStateException();
                    }
                }
            });
        }
        else
        {
            return Either.left(validationResult.getError().getOrNull());
        }
    }

    @Override
    public ServiceResult setPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey, @Nonnull final String value, final Option<ETag> eTag)
    {
        Preconditions.checkArgument(value.length() <= MAXIMUM_PROPERTY_VALUE_LENGTH);
        ValidationResult<SetPropertyInput> validationResult = validateSetPropertyValue(user, sourceAddOnKey, checkNotNull(addOnKey), checkNotNull(propertyKey), checkNotNull(value));
        if (validationResult.isValid())
        {
            SetPropertyInput input = validationResult.getValue().getOrNull();
            AddOnPropertyStore.PutResult putResult = store.setPropertyValue(input.getAddOnKey(), input.getPropertyKey(), input.getValue(), eTag);
            switch (putResult)
            {
                case PROPERTY_CREATED: return ServiceResultImpl.PROPERTY_CREATED;
                case PROPERTY_UPDATED: return ServiceResultImpl.PROPERTY_UPDATED;
                case PROPERTY_LIMIT_EXCEEDED: return ServiceResultImpl.MAXIMUM_PROPERTIES_EXCEEDED;
                case PROPERTY_MODIFIED: return ServiceResultImpl.PROPERTY_MODIFIED;
                default:
                {
                    log.error("PutResult case not covered. Method setPropertyValue has returned an enum for which there is no corresponding ServiceResult.");
                    throw new IllegalStateException("PutResult case not covered.");
                }
            }
        }
        else
        {
            return validationResult.getError().getOrNull();
        }
    }

    @Override
    public ServiceResult deletePropertyValue(@Nullable final UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey, @Nonnull Option<ETag> eTag)
    {
        ValidationResult<GetOrDeletePropertyInput> validationResult = validateDeletePropertyValue(user, sourceAddOnKey, checkNotNull(addOnKey), checkNotNull(propertyKey));
        if (validationResult.isValid())
        {
            GetOrDeletePropertyInput input = validationResult.getValue().getOrNull();
            AddOnPropertyStore.DeleteResult deleteResult = store.deletePropertyValue(input.addOnKey, input.propertyKey, eTag);
            switch (deleteResult)
            {
                case PROPERTY_DELETED: return ServiceResultImpl.PROPERTY_DELETED;
                case PROPERTY_NOT_FOUND: return ServiceResultImpl.PROPERTY_NOT_FOUND;
                case PROPERTY_MODIFIED: return ServiceResultImpl.PROPERTY_MODIFIED;
                default:
                {
                    log.error("DeleteResult case not covered. Method deletePropertyValue has returned an enum for which there is no corresponding ServiceResult.");
                    throw new IllegalStateException("DeleteResult case not covered.");
                }
            }
        }
        else
        {
            return validationResult.getError().get();
        }
    }

    @Override
    public Either<ServiceResult, AddOnPropertyIterable> getAddOnProperties(@Nullable final UserProfile user, @Nullable final String sourcePluginKey, @Nonnull final String addOnKey, @Nonnull final Option<ETag> eTag)
    {
        ValidationResult<String> validationResult = validateListProperties(user, sourcePluginKey, checkNotNull(addOnKey));
        if (validationResult.isValid())
        {
            String input = validationResult.getValue().get();
            Either<AddOnPropertyStore.ListResult, AddOnPropertyIterable> listPropertiesResult = store.getAllPropertiesForAddOnKey(input, eTag);

            return listPropertiesResult.left().map(new Function<AddOnPropertyStore.ListResult, ServiceResult>()
            {
                @Override
                public ServiceResult apply(final AddOnPropertyStore.ListResult input)
                {
                    switch (input)
                    {
                        case PROPERTIES_NOT_MODIFIED:
                            return ServiceResultImpl.PROPERTIES_NOT_MODIFIED;
                    }
                    throw new IllegalStateException();
                }
            });
        }
        return Either.left(validationResult.getError().get());
    }

    private Option<ServiceResult> validateCommonParameters(UserProfile user, String sourceAddOnKey, String addOnKey, String propertyKey)
    {
        if (!loggedIn(user))
        {
            return Option.<ServiceResult>some(ServiceResultImpl.NOT_AUTHENTICATED);
        }
        if (!hasAccessToData(user, sourceAddOnKey, addOnKey))
        {
            return Option.<ServiceResult>some(ServiceResultImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        if (propertyKey.length() > AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH)
        {
            return Option.<ServiceResult>some(ServiceResultImpl.KEY_TOO_LONG);
        }
        if (!connectAddonRegistry.hasAddonWithKey(addOnKey))
        {
            return Option.<ServiceResult>some(ServiceResultImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        return Option.none();
    }

    private ValidationResult<GetOrDeletePropertyInput> validateGetPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        Option<ServiceResult> validationError = validateCommonParameters(user, sourceAddOnKey, addOnKey, propertyKey);
        if (validationError.isDefined())
        {
            return ValidationResult.fromError(validationError.get());
        }
        return ValidationResult.fromValue(new GetOrDeletePropertyInput(addOnKey, propertyKey));
    }

    private ValidationResult<SetPropertyInput> validateSetPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, final String propertyKey, final String value)
    {
        Option<ServiceResult> validationError = validateCommonParameters(user, sourceAddOnKey, addOnKey, propertyKey);
        if (validationError.isDefined())
        {
            return ValidationResult.fromError(validationError.get());
        }
        if (!isJSONValid(value))
        {
            return ValidationResult.fromError(ServiceResultImpl.INVALID_PROPERTY_VALUE);
        }
        return ValidationResult.fromValue(new SetPropertyInput(addOnKey, propertyKey, value));
    }

    private ValidationResult<GetOrDeletePropertyInput> validateDeletePropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        Option<ServiceResult> validationError = validateCommonParameters(user, sourceAddOnKey, addOnKey, propertyKey);
        if (validationError.isDefined())
        {
            return ValidationResult.fromError(validationError.get());
        }
        return ValidationResult.fromValue(new GetOrDeletePropertyInput(addOnKey, propertyKey));
    }

    private ValidationResult<String> validateListProperties(final UserProfile user, final String sourcePluginKey, final String addOnKey)
    {
        if (!loggedIn(user))
        {
            return ValidationResult.fromError(ServiceResultImpl.NOT_AUTHENTICATED);
        }
        if (!hasAccessToData(user, sourcePluginKey, addOnKey))
        {
            return ValidationResult.fromError(ServiceResultImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        if (!connectAddonRegistry.hasAddonWithKey(addOnKey))
        {
            return ValidationResult.fromError(ServiceResultImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        return ValidationResult.fromValue(addOnKey);
    }

    private boolean hasAccessToData(final UserProfile user, final String sourcePluginKey, final String addOnKey)
    {
        return pluginHasPermissions(sourcePluginKey, addOnKey) || (sourcePluginKey == null && isSysAdmin(user));
    }

    private boolean isSysAdmin(final UserProfile user)
    {
        return userManager.isSystemAdmin(user.getUserKey());
    }

    private boolean loggedIn(final UserProfile user)
    {
        return user != null;
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
