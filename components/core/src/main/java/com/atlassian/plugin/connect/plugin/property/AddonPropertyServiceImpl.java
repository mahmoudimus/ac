package com.atlassian.plugin.connect.plugin.property;

import com.atlassian.plugin.connect.api.property.AddonProperty;
import com.atlassian.plugin.connect.api.property.AddonPropertyService;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.property.AddonPropertyStore.PutResultWithOptionalProperty;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpStatus;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

import static com.atlassian.plugin.connect.plugin.property.AddonPropertyStore.MAX_PROPERTIES_PER_ADD_ON;
import static com.atlassian.plugin.connect.plugin.property.AddonPropertyStore.PutResult;
import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class AddonPropertyServiceImpl implements AddonPropertyService {

    private static final Logger log = LoggerFactory.getLogger(AddonPropertyServiceImpl.class);

    public enum OperationStatusImpl implements AddonPropertyService.OperationStatus {
        PROPERTY_UPDATED(HttpStatus.SC_OK, "connect.rest.add_on_properties.property_updated"),
        PROPERTY_CREATED(HttpStatus.SC_CREATED, "connect.rest.add_on_properties.property_created"),
        KEY_TOO_LONG(HttpStatus.SC_BAD_REQUEST, "connect.rest.add_on_properties.key_too_long", String.valueOf(AddonPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH)),
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

        OperationStatusImpl(int httpStatusCode, String i18nKey) {
            this(httpStatusCode, i18nKey, new String[0]);
        }

        OperationStatusImpl(int httpStatusCode, String i18nKey, String... values) {
            this.httpStatusCode = httpStatusCode;
            this.i18nKey = i18nKey;
            this.values = values;
        }

        public int getHttpStatusCode() {
            return httpStatusCode;
        }

        public String message(I18nResolver resolver) {
            if (i18nKey == null) {
                return null;
            }
            return resolver.getText(i18nKey, values);
        }

        public String getKey() {
            return i18nKey;
        }
    }

    public static final int MAXIMUM_PROPERTY_VALUE_LENGTH = 32 * 1024; //32KB

    private final AddonPropertyStore store;
    private final UserManager userManager;
    private final ConnectAddonRegistry connectAddonRegistry;

    @Autowired
    public AddonPropertyServiceImpl(AddonPropertyStore store, UserManager userManager, ConnectAddonRegistry connectAddonRegistry) {
        this.store = checkNotNull(store);
        this.userManager = checkNotNull(userManager);
        this.connectAddonRegistry = checkNotNull(connectAddonRegistry);
    }

    @Override
    public GetServiceResult getPropertyValue(
            @Nullable UserProfile user,
            @Nullable final String sourceAddonKey,
            @Nonnull final String addonKey,
            @Nonnull final String propertyKey) {
        ValidationResult<GetOrDeletePropertyInput> validationResult = validateGetPropertyValue(user, sourceAddonKey, addonKey, propertyKey);
        if (validationResult.isValid()) {
            GetOrDeletePropertyInput input = validationResult.getValue().orElse(null);
            Optional<AddonProperty> propertyValue = store.getPropertyValue(input.getAddonKey(), input.getPropertyKey());
            if (!propertyValue.isPresent()) {
                return new GetServiceResult.Fail(OperationStatusImpl.PROPERTY_NOT_FOUND);
            } else {
                return new GetServiceResult.Success(propertyValue.get());
            }
        } else {
            return new GetServiceResult.Fail(validationResult.getError().get());
        }
    }

    @Override
    public <T> PutServiceResult<T> setPropertyValueIfConditionSatisfied(
            @Nullable UserProfile user,
            @Nullable final String sourceAddonKey,
            @Nonnull final String addonKey,
            @Nonnull final String propertyKey,
            @Nonnull final String value,
            @Nonnull final Function<Optional<AddonProperty>, ServiceConditionResult<T>> testFunction) {
        Preconditions.checkArgument(value.length() <= MAXIMUM_PROPERTY_VALUE_LENGTH);
        ValidationResult<SetPropertyInput> validationResult = validateSetPropertyValue(user, sourceAddonKey, checkNotNull(addonKey), checkNotNull(propertyKey), checkNotNull(value));
        if (validationResult.isValid()) {
            final SetPropertyInput input = validationResult.getValue().orElse(null);

            return store.executeInTransaction(() -> {
                Optional<AddonProperty> propertyOption = store.getPropertyValue(input.getAddonKey(), input.getPropertyKey());

                final ServiceConditionResult<T> conditionSucceed = testFunction.apply(propertyOption);

                if (conditionSucceed.isSuccessful()) {
                    final PutResultWithOptionalProperty putResult = store.setPropertyValue(input.getAddonKey(), input.getPropertyKey(), input.getValue());
                    if (putResult.getProperty().isPresent()) {
                        return new PutServiceResult.Success<T>(
                                new PutOperationStatus(
                                        StoreToServiceResultMapping.mapToServiceResult(putResult.getResult()),
                                        putResult.getProperty().get()));
                    }
                    return new PutServiceResult.Fail<T>(StoreToServiceResultMapping.mapToServiceResult(putResult.getResult()));
                } else {
                    return new PutServiceResult.PreconditionFail<T>(conditionSucceed.getObject().get());
                }
            });
        } else {
            return new PutServiceResult.Fail<T>(validationResult.getError().get());
        }
    }

    @Override
    public <T> DeleteServiceResult<T> deletePropertyValueIfConditionSatisfied(
            @Nullable final UserProfile user,
            @Nullable final String sourceAddonKey,
            @Nonnull final String addonKey,
            @Nonnull final String propertyKey,
            @Nonnull final Function<Optional<AddonProperty>, ServiceConditionResult<T>> testFunction) {
        ValidationResult<GetOrDeletePropertyInput> validationResult = validateDeletePropertyValue(user, sourceAddonKey, checkNotNull(addonKey), checkNotNull(propertyKey));
        if (validationResult.isValid()) {
            final GetOrDeletePropertyInput input = validationResult.getValue().orElse(null);

            return store.executeInTransaction(() -> {
                final Optional<AddonProperty> propertyValue = store.getPropertyValue(input.getAddonKey(), input.getPropertyKey());
                if (!propertyValue.isPresent()) {
                    return new DeleteServiceResult.Fail<T>(OperationStatusImpl.PROPERTY_NOT_FOUND);
                }

                final ServiceConditionResult<T> conditionSucceeded = testFunction.apply(propertyValue);
                if (conditionSucceeded.isSuccessful()) {
                    store.deletePropertyValue(input.getAddonKey(), input.getPropertyKey());
                    return new DeleteServiceResult.Success<T>(OperationStatusImpl.PROPERTY_DELETED);
                } else {
                    return new DeleteServiceResult.PreconditionFail<T>(conditionSucceeded.getObject().get());
                }
            });
        } else {
            return new DeleteServiceResult.Fail<T>(validationResult.getError().get());
        }
    }

    @Override
    public GetAllServiceResult getAddOnProperties(@Nonnull final String addOnKey) {
        ValidationResult<String> validationResult = validateIfAddOnExists(checkNotNull(addOnKey));
        return getAddOnProperties(validationResult);
    }

    @Override
    public GetAllServiceResult getAddonProperties(@Nullable final UserProfile user, @Nullable final String sourcePluginKey, @Nonnull final String addonKey) {
        ValidationResult<String> validationResult = validateListProperties(user, sourcePluginKey, checkNotNull(addonKey));
        return getAddOnProperties(validationResult);
    }

    private GetAllServiceResult getAddOnProperties(final ValidationResult<String> validatedAddOnKey) {
        if (validatedAddOnKey.isValid()) {
            String addOnKey = validatedAddOnKey.getValue().get();
            return new GetAllServiceResult.Success(store.getAllPropertiesForAddonKey(addOnKey));
        }
        return new GetAllServiceResult.Fail(validatedAddOnKey.getError().get());
    }

    private Optional<OperationStatus> validateCommonParameters(UserProfile user, String sourceAddonKey, String addonKey, String propertyKey) {
        if (!loggedIn(user)) {
            return Optional.of(OperationStatusImpl.NOT_AUTHENTICATED);
        }
        if (!hasAccessToData(user, sourceAddonKey, addonKey)) {
            return Optional.of(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        if (propertyKey.length() > AddonPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH) {
            return Optional.of(OperationStatusImpl.KEY_TOO_LONG);
        }
        if (!connectAddonRegistry.hasAddonWithKey(addonKey)) {
            return Optional.of(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        return Optional.empty();
    }

    private ValidationResult<GetOrDeletePropertyInput> validateGetPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddonKey, @Nonnull final String addonKey, @Nonnull final String propertyKey) {
        Optional<OperationStatus> validationError = validateCommonParameters(user, sourceAddonKey, addonKey, propertyKey);
        if (validationError.isPresent()) {
            return ValidationResult.fromError(validationError.get());
        }
        return ValidationResult.fromValue(new GetOrDeletePropertyInput(addonKey, propertyKey));
    }

    private ValidationResult<SetPropertyInput> validateSetPropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddonKey, @Nonnull final String addonKey, final String propertyKey, final String value) {
        Optional<OperationStatus> validationError = validateCommonParameters(user, sourceAddonKey, addonKey,
                propertyKey);
        if (validationError.isPresent()) {
            return ValidationResult.fromError(validationError.get());
        }
        if (!isJSONValid(value)) {
            return ValidationResult.fromError(OperationStatusImpl.INVALID_PROPERTY_VALUE);
        }
        return ValidationResult.fromValue(new SetPropertyInput(addonKey, propertyKey, value));
    }

    private ValidationResult<GetOrDeletePropertyInput> validateDeletePropertyValue(@Nullable UserProfile user, @Nullable final String sourceAddonKey, @Nonnull final String addonKey, @Nonnull final String propertyKey) {
        Optional<OperationStatus> validationError = validateCommonParameters(user, sourceAddonKey, addonKey,
                propertyKey);
        if (validationError.isPresent()) {
            return ValidationResult.fromError(validationError.get());
        }
        return ValidationResult.fromValue(new GetOrDeletePropertyInput(addonKey, propertyKey));
    }

    private ValidationResult<String> validateListProperties(final UserProfile user, final String sourcePluginKey, final String addonKey) {
        if (!loggedIn(user)) {
            return ValidationResult.fromError(OperationStatusImpl.NOT_AUTHENTICATED);
        }
        if (!hasAccessToData(user, sourcePluginKey, addonKey)) {
            return ValidationResult.fromError(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        if (!connectAddonRegistry.hasAddonWithKey(addonKey)) {
            return ValidationResult.fromError(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
        }
        return validateIfAddOnExists(addonKey);
    }

    private ValidationResult<String> validateIfAddOnExists(String addonKey) {
        return ValidationResult.fromValue(addonKey);
    }

    private boolean hasAccessToData(final UserProfile user, final String sourcePluginKey, final String addonKey) {
        return pluginHasPermissions(sourcePluginKey, addonKey) || (sourcePluginKey == null && isSysAdmin(user));
    }

    private boolean isSysAdmin(final UserProfile user) {
        return userManager.isSystemAdmin(user.getUserKey());
    }

    private boolean loggedIn(final UserProfile user) {
        return user != null;
    }

    private boolean pluginHasPermissions(String requestKey, String addonKey) {
        return requestKey != null && requestKey.equals(addonKey);
    }

    private class GetOrDeletePropertyInput {
        final String addonKey;
        final String propertyKey;

        public GetOrDeletePropertyInput(final String addonKey, final String propertyKey) {
            this.addonKey = addonKey;
            this.propertyKey = propertyKey;
        }

        public String getAddonKey() {
            return addonKey;
        }

        public String getPropertyKey() {
            return propertyKey;
        }
    }

    private class SetPropertyInput {
        final GetOrDeletePropertyInput input;
        final String value;

        public SetPropertyInput(final String addonKey, final String propertyKey, final String value) {
            this.input = new GetOrDeletePropertyInput(addonKey, propertyKey);
            this.value = value;
        }

        public String getAddonKey() {
            return input.getAddonKey();
        }

        public String getPropertyKey() {
            return input.getPropertyKey();
        }

        public String getValue() {
            return value;
        }
    }

    private boolean isJSONValid(String jsonString) {
        try {
            JSONParser parser = new JSONParser();
            parser.parse(jsonString);
            return true;
        } catch (ParseException e) {
            log.debug("Invalid json when setting property value for plugin.");
            return false;
        }
    }

    private static class StoreToServiceResultMapping {
        private static final Map<PutResult, OperationStatus> mapping =
                ImmutableMap.<PutResult, OperationStatus>builder()
                        .put(PutResult.PROPERTY_CREATED, OperationStatusImpl.PROPERTY_CREATED)
                        .put(PutResult.PROPERTY_LIMIT_EXCEEDED, OperationStatusImpl.MAXIMUM_PROPERTIES_EXCEEDED)
                        .put(PutResult.PROPERTY_UPDATED, OperationStatusImpl.PROPERTY_UPDATED)
                        .build();

        public static OperationStatus mapToServiceResult(PutResult putResult) {
            if (mapping.containsKey(putResult)) {
                return mapping.get(putResult);
            }
            log.error("StoreResult case not covered. Method in AddonPropertyStore has returned an enum for which there is no corresponding ServiceResult.");
            throw new IllegalStateException("StoreResult case not covered.");
        }
    }
}
