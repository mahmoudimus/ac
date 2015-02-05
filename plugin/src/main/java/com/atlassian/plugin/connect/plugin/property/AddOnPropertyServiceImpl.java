package com.atlassian.plugin.connect.plugin.property;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Suppliers;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyAO;
import com.atlassian.plugin.connect.plugin.property.AddOnPropertyStore.PutResultWithOptionalProperty;
import com.atlassian.plugin.connect.plugin.property.AddOnPropertyStore.TransactionAction;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.httpclient.HttpStatus;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.property.AddOnPropertyStore.MAX_PROPERTIES_PER_ADD_ON;
import static com.atlassian.plugin.connect.plugin.property.AddOnPropertyStore.PutResult;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since TODO: fill in the proper version before merge
 */
@Component
public class AddOnPropertyServiceImpl implements AddOnPropertyService
{

    private static final Logger log = LoggerFactory.getLogger(AddOnPropertyServiceImpl.class);

    public enum OperationStatusImpl implements OperationStatus
    {
        PROPERTY_UPDATED(HttpStatus.SC_OK, "connect.rest.add_on_properties.property_updated"),
        PROPERTY_CREATED(HttpStatus.SC_CREATED, "connect.rest.add_on_properties.property_created"),
        KEY_TOO_LONG(HttpStatus.SC_BAD_REQUEST, "connect.rest.add_on_properties.key_too_long", String.valueOf(AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH)),
        MAXIMUM_PROPERTIES_EXCEEDED(HttpStatus.SC_CONFLICT, "connect.rest.add_on_properties.maximum_properties_exceeded", String.valueOf(MAX_PROPERTIES_PER_ADD_ON)),
        PROPERTY_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "connect.rest.add_on_properties.property_not_found"),
        INVALID_PROPERTY_VALUE(HttpStatus.SC_BAD_REQUEST, "connect.rest.add_on_properties.invalid_property_value"),
        NOT_AUTHENTICATED(HttpStatus.SC_UNAUTHORIZED, "connect.rest.add_on_properties.not_authenticated"),
        PROPERTY_DELETED(HttpStatus.SC_NO_CONTENT, null),
        ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN(HttpStatus.SC_NOT_FOUND, "connect.rest.add_on_properties.add_on_not_found_or_access_to_other_data_forbidden"),
        PROPERTY_MODIFIED(HttpStatus.SC_PRECONDITION_FAILED, "connect.rest.add_on_properties.property_modified");

        private final int httpStatusCode;
        private final String i18nKey;
        private final String[] values;

        private OperationStatusImpl(int httpStatusCode, String i18nKey)
        {
            this(httpStatusCode, i18nKey, new String[0]);
        }

        private OperationStatusImpl(int httpStatusCode, String i18nKey, String... values)
        {
            this.httpStatusCode = httpStatusCode;
            this.i18nKey = i18nKey;
            this.values = values;
        }

        public int getHttpStatusCode()
        {
            return httpStatusCode;
        }

        public String message(I18nResolver resolver)
        {
            if (i18nKey == null)
            {
                return null;
            }
            return resolver.getText(i18nKey, values);
        }

        public String getKey()
        {
            return i18nKey;
        }
    }

    public static final int MAXIMUM_PROPERTY_VALUE_LENGTH = 32*1024; //32KB

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
    public GetServiceResult getPropertyValue(
            @Nullable UserProfile user,
            @Nullable final String sourceAddOnKey,
            @Nonnull final String addOnKey,
            @Nonnull final String propertyKey)
    {
        ValidationResult<GetOrDeletePropertyInput> validationResult = validateGetPropertyValue(user, sourceAddOnKey, addOnKey, propertyKey);
        if (validationResult.isValid())
        {
            GetOrDeletePropertyInput input = validationResult.getValue().getOrNull();
            Option<AddOnProperty> propertyValue = store.getPropertyValue(input.getAddOnKey(), input.getPropertyKey());
            return propertyValue.fold(
                    Suppliers.ofInstance(new GetServiceResult.Fail(OperationStatusImpl.PROPERTY_NOT_FOUND)),
                    new Function<AddOnProperty, GetServiceResult>()
            {
                @Override
                public GetServiceResult apply(@Nullable final AddOnProperty input)
                {
                    return new GetServiceResult.Success(input);
                }
            });
        }
        else
        {
            return new GetServiceResult.Fail(validationResult.getError().get());
        }
    }

    @Override
    public <T> PutServiceResult<T> setPropertyValueIfConditionSatisfied(
            @Nullable UserProfile user,
            @Nullable final String sourceAddOnKey,
            @Nonnull final String addOnKey,
            @Nonnull final String propertyKey,
            @Nonnull final String value,
            @Nonnull final Function<Option<AddOnProperty>, ServiceConditionResult<T>> testFunction)
    {
        Preconditions.checkArgument(value.length() <= MAXIMUM_PROPERTY_VALUE_LENGTH);
        ValidationResult<SetPropertyInput> validationResult = validateSetPropertyValue(user, sourceAddOnKey, checkNotNull(addOnKey), checkNotNull(propertyKey), checkNotNull(value));
        if (validationResult.isValid())
        {
            final SetPropertyInput input = validationResult.getValue().getOrNull();

            return store.executeInTransaction(new TransactionAction<PutServiceResult<T>>()
            {
                @Override
                public PutServiceResult<T> call()
                {
                    Option<AddOnProperty> propertyOption = store.getPropertyValue(input.getAddOnKey(), input.getPropertyKey());

                    final ServiceConditionResult<T> conditionSucceed = testFunction.apply(propertyOption);

                    if (conditionSucceed.isSuccessful())
                    {
                        final PutResultWithOptionalProperty putResult = store.setPropertyValue(input.getAddOnKey(), input.getPropertyKey(), input.getValue());
                        if (putResult.getProperty().isDefined())
                        {
                            return new PutServiceResult.Success<T>(
                                    new PutOperationStatus(
                                        StoreToServiceResultMapping.mapToServiceResult(putResult.getResult()),
                                        putResult.getProperty().get()));
                        }
                        return new PutServiceResult.Fail<T>(StoreToServiceResultMapping.mapToServiceResult(putResult.getResult()));
                    }
                    else
                    {
                        return new PutServiceResult.PreconditionFail<T>(conditionSucceed.getObject().get());
                    }
                }
            });
        }
        else
        {
            return new PutServiceResult.Fail<T>(validationResult.getError().get());
        }
    }

    @Override
    public <T> DeleteServiceResult<T> deletePropertyValueIfConditionSatisfied(
            @Nullable final UserProfile user,
            @Nullable final String sourceAddOnKey,
            @Nonnull final String addOnKey,
            @Nonnull final String propertyKey,
            @Nonnull final Function<Option<AddOnProperty>, ServiceConditionResult<T>> testFunction)
    {
        ValidationResult<GetOrDeletePropertyInput> validationResult = validateDeletePropertyValue(user, sourceAddOnKey, checkNotNull(addOnKey), checkNotNull(propertyKey));
        if (validationResult.isValid())
        {
            final GetOrDeletePropertyInput input = validationResult.getValue().getOrNull();

            return store.executeInTransaction(new TransactionAction<DeleteServiceResult<T>>()
            {
                @Override
                public DeleteServiceResult<T> call()
                {
                    return store.getPropertyValue(input.getAddOnKey(), input.getPropertyKey()).fold(new Supplier<DeleteServiceResult<T>>()
                    {
                        @Override
                        public DeleteServiceResult<T> get()
                        {
                            return new DeleteServiceResult.Fail<T>(OperationStatusImpl.PROPERTY_NOT_FOUND);
                        }
                    }, new Function<AddOnProperty, DeleteServiceResult<T>>()
                    {
                        @Override
                        public DeleteServiceResult<T> apply(final AddOnProperty property)
                        {
                            final ServiceConditionResult<T> conditionSucceeded = testFunction.apply(Option.some(property));
                            if (conditionSucceeded.isSuccessful())
                            {
                                store.deletePropertyValue(input.getAddOnKey(), input.getPropertyKey());
                                return new DeleteServiceResult.Success<T>(OperationStatusImpl.PROPERTY_DELETED);
                            }
                            else
                            {
                                return new DeleteServiceResult.PreconditionFail<T>(conditionSucceeded.getObject().get());
                            }
                        }
                    });
                }
            });
        }
        else
        {
            return new DeleteServiceResult.Fail<T>(validationResult.getError().get());
        }
    }

    @Override
    public GetAllServiceResult getAddOnProperties(@Nullable final UserProfile user, @Nullable final String sourcePluginKey, @Nonnull final String addOnKey)
    {
        ValidationResult<String> validationResult = validateListProperties(user, sourcePluginKey, checkNotNull(addOnKey));
        if (validationResult.isValid())
        {
            String input = validationResult.getValue().get();
            return new GetAllServiceResult.Success(store.getAllPropertiesForAddOnKey(input));
        }
        return new GetAllServiceResult.Fail(validationResult.getError().get());
    }

    private Option<OperationStatus> validateCommonParameters(UserProfile user, String sourceAddOnKey, String addOnKey, String propertyKey)
    {
        if (!loggedIn(user))
        {
            return Option.<OperationStatus>some(OperationStatusImpl.NOT_AUTHENTICATED);
        }
        if (!hasAccessToData(user, sourceAddOnKey, addOnKey))
        {
            return Option.<OperationStatus>some(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        if (propertyKey.length() > AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH)
        {
            return Option.<OperationStatus>some(OperationStatusImpl.KEY_TOO_LONG);
        }
        if (!connectAddonRegistry.hasAddonWithKey(addOnKey))
        {
            return Option.<OperationStatus>some(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        return Option.none();
    }

    private ValidationResult<GetOrDeletePropertyInput> validateGetPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        Option<OperationStatus> validationError = validateCommonParameters(user, sourceAddOnKey, addOnKey, propertyKey);
        if (validationError.isDefined())
        {
            return ValidationResult.fromError(validationError.get());
        }
        return ValidationResult.fromValue(new GetOrDeletePropertyInput(addOnKey, propertyKey));
    }

    private ValidationResult<SetPropertyInput> validateSetPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, final String propertyKey, final String value)
    {
        Option<OperationStatus> validationError = validateCommonParameters(user, sourceAddOnKey, addOnKey, propertyKey);
        if (validationError.isDefined())
        {
            return ValidationResult.fromError(validationError.get());
        }
        if (!isJSONValid(value))
        {
            return ValidationResult.fromError(OperationStatusImpl.INVALID_PROPERTY_VALUE);
        }
        return ValidationResult.fromValue(new SetPropertyInput(addOnKey, propertyKey, value));
    }

    private ValidationResult<GetOrDeletePropertyInput> validateDeletePropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddOnKey, @Nonnull final String addOnKey, @Nonnull final String propertyKey)
    {
        Option<OperationStatus> validationError = validateCommonParameters(user, sourceAddOnKey, addOnKey, propertyKey);
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
            return ValidationResult.fromError(OperationStatusImpl.NOT_AUTHENTICATED);
        }
        if (!hasAccessToData(user, sourcePluginKey, addOnKey))
        {
            return ValidationResult.fromError(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        if (!connectAddonRegistry.hasAddonWithKey(addOnKey))
        {
            return ValidationResult.fromError(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
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
            JSONParser parser = new JSONParser();
            parser.parse(jsonString);
            return true;
        }
        catch (ParseException e) {
            log.debug("Invalid json when setting property value for plugin.");
            return false;
        }
    }

    private static class StoreToServiceResultMapping
    {
        private static final Map<PutResult, OperationStatus> mapping =
                ImmutableMap.<PutResult, OperationStatus>builder()
                        .put(PutResult.PROPERTY_CREATED, OperationStatusImpl.PROPERTY_CREATED)
                        .put(PutResult.PROPERTY_LIMIT_EXCEEDED, OperationStatusImpl.MAXIMUM_PROPERTIES_EXCEEDED)
                        .put(PutResult.PROPERTY_UPDATED, OperationStatusImpl.PROPERTY_UPDATED)
                        .build();

        public static OperationStatus mapToServiceResult(PutResult putResult)
        {
            if (mapping.containsKey(putResult))
            {
                return mapping.get(putResult);
            }
            log.error("StoreResult case not covered. Method in AddOnPropertyStore has returned an enum for which there is no corresponding ServiceResult.");
            throw new IllegalStateException("StoreResult case not covered.");
        }
    }
}
