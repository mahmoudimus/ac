package com.atlassian.plugin.connect.api.property;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.atlassian.annotations.PublicApi;
import com.atlassian.fugue.Either;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Function;

/**
 * This service is used to add, remove, list and update add-on properties.
 * Checks permissions and constraints on input before executing an action.
 */
@PublicApi
public interface AddonPropertyService {
    /**
     * Gets a property from the add-on store. <p> This method checks parameter validity and tries to get a property for
     * an add-on. </p>
     *
     * @param user the user performing the request
     * @param sourcePluginKey the key of the add-on performing the request
     * @param addonKey the key of the add-on that owns the property
     * @param propertyKey the key of the property
     * @return GetServiceResult which calls one of two callbacks: OnFailed or OnSuccess,
     * OnFailed is called with OperationResult explaining the reason
     * OnSuccess is called with AddonProperty that was retrieved.
     */
    GetServiceResult getPropertyValue(
            @Nullable UserProfile user,
            @Nullable String sourcePluginKey,
            @Nonnull String addonKey,
            @Nonnull String propertyKey);

    /**
     * Sets a property from the add-on store. <p> This method checks parameter validity and tries to set a property for
     * an add-on. </p>
     *
     * @param user the user performing the request
     * @param sourcePluginKey the key of the add-on performing the request
     * @param addonKey the key of the add-on that owns the property
     * @param propertyKey the key of the property
     * @param value the value of the property
     * @param testFunction a condition that must pass for the property to be stored
     * @param <T> type of object which is the reason of testFunction failure
     * @return PutServiceResult which calls one of three callbacks: onPreconditionFailed, OnFailed or OnSuccess
     * PreconditionFailed is called with an object of type T that was the result of testFunction failure.
     * OnFailed is called with OperationResult explaining the reason of failure
     * OnSuccess is called with OperationPutResult explaining the reason of success and the resulting AddonProperty
     */
    <T> PutServiceResult<T> setPropertyValueIfConditionSatisfied(
            @Nullable UserProfile user,
            @Nullable String sourcePluginKey,
            @Nonnull String addonKey,
            @Nonnull String propertyKey,
            @Nonnull String value,
            @Nonnull final Function<Optional<AddonProperty>, ServiceConditionResult<T>> testFunction);

    /**
     * Deletes a property from the add-on store. <p> This method checks parameter validity and tries to delete a
     * property for an add-on. </p>
     *
     * @param user the user performing the request
     * @param sourcePluginKey the key of the add-on performing the request
     * @param addonKey the key of the add-on that owns the property
     * @param propertyKey the key of the property
     * @param testFunction a condition that must pass for the property to be deleted
     * @param <T> type of object which is the reason of testFunction failure
     * @return DeleteServiceResult which calls one of three callbacks: onPreconditionFailed, OnFailed or OnSuccess
     * PreconditionFailed is called with an object of type T that was the result of testFunction failure.
     * OnFailed is called with OperationResult explaining the reason of failure
     * OnSuccess is called with OperationResult explaining the reason of success
     */
    <T> DeleteServiceResult<T> deletePropertyValueIfConditionSatisfied(
            @Nullable UserProfile user,
            @Nullable String sourcePluginKey,
            @Nonnull String addonKey,
            @Nonnull String propertyKey,
            @Nonnull final Function<Optional<AddonProperty>, ServiceConditionResult<T>> testFunction);

    /**
     * Returns a list of all properties for a given add-on. <p> This method checks parameter validity and lists all
     * properties belonging to an add-on. </p>
     *
     * @param user the user performing the request
     * @param sourcePluginKey the key of the add-on performing the request
     * @param addonKey the key of the add-on that owns the property
     * @return GetAllServiceResult which calls one of two callbacks: OnFailed or OnSuccess,
     * OnFailed is called with OperationResult explaining the reason
     * OnSuccess is called with AddonPropertyIterable that was retrieved.
     */
    GetAllServiceResult getAddonProperties(@Nullable UserProfile user, @Nullable String sourcePluginKey, @Nonnull String addonKey);

    /**
     * Returns a list of all properties for a given add-on. This method does not check any permissions, but checks whether the add-on exists.
     *
     * @param addOnKey the key of the add-on that owns the property
     * @return GetAllServiceResult which calls one of two callbacks: OnFailed or OnSuccess,
     * OnFailed is called with OperationResult explaining the reason
     * OnSuccess is called with AddOnPropertyIterable that was retrieved.
     */
    GetAllServiceResult getAddOnProperties(@Nonnull String addOnKey);

    /**
     * Represents a result of the Get operation in service. Can be folded by giving two functions, which are called depending on the result.
     * @param <T> type of object which is passed to onSuccess function.
     */
    interface FoldableGetServiceResult<T> {
        public <R> R fold(Function<OperationStatus, R> onFail,
                          Function<T, R> onSuccess);
    }

    /**
     * Represents a result of an operation in service. Can be folded by giving three functions, which are called depending on the result.
     * @param <T> type of object which is passed to onSuccess function.
     * @param <SRT> type of object passed to onSuccess function.
     */
    interface FoldableServiceResult<T, SRT extends OperationStatus> {
        public <R> R fold(Function<T, R> onPreconditionFailed,
                          Function<OperationStatus, R> onFail,
                          Function<SRT, R> onSuccess);
    }

    /**
     * Represents a status of the operation. Contains an httpStatus code and I18N key and message.
     */
    interface OperationStatus {
        public int getHttpStatusCode();

        public String message(I18nResolver resolver);

        public String getKey();
    }

    /**
     * Represents a condition result which is a boolean along with an optional object
     * @param <T> type of optional object
     */
    @Immutable
    class ServiceConditionResult<T> {
        private final Optional<T> object;

        private final boolean successful;

        private ServiceConditionResult(Optional<T> object, boolean isSuccessful) {
            this.object = object;
            this.successful = isSuccessful;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public Optional<T> getObject() {
            return object;
        }

        public static <T> ServiceConditionResult<T> SUCCESS() {
            return new ServiceConditionResult<T>(null, true);
        }

        public static <T> ServiceConditionResult<T> FAILURE_WITH_OBJECT(T obj) {
            return new ServiceConditionResult<T>(Optional.of(obj), false);
        }
    }

    @Immutable
    class ValidationResult<T> {
        private final Either<OperationStatus, T> result;

        public ValidationResult(Either<OperationStatus, T> result) {
            this.result = result;
        }

        public boolean isValid() {
            return result.isRight();
        }

        public Optional<OperationStatus> getError() {
            return Optional.ofNullable(result.left().getOrNull());
        }

        public Optional<T> getValue() {
            return Optional.ofNullable(result.right().getOrNull());
        }

        public static <T> ValidationResult<T> fromValue(T value) {
            return new ValidationResult<T>(Either.<OperationStatus, T>right(value));
        }

        public static <T> ValidationResult<T> fromError(OperationStatus error) {
            return new ValidationResult<T>(Either.<OperationStatus, T>left(error));
        }
    }

    interface GetServiceResult extends FoldableGetServiceResult<AddonProperty> {
        class Fail implements GetServiceResult {
            private final OperationStatus reason;

            public Fail(OperationStatus reason) {
                this.reason = reason;
            }

            @Override
            public <R> R fold(final Function<OperationStatus, R> onFail, final Function<AddonProperty, R> onSuccess) {
                return onFail.apply(reason);
            }
        }

        class Success implements GetServiceResult {
            private final AddonProperty property;

            public Success(AddonProperty property) {
                this.property = property;
            }

            @Override
            public <R> R fold(final Function<OperationStatus, R> onFail, final Function<AddonProperty, R> onSuccess) {
                return onSuccess.apply(property);
            }
        }
    }

    interface GetAllServiceResult extends FoldableGetServiceResult<AddonPropertyIterable> {
        class Fail implements GetAllServiceResult {
            private final OperationStatus reason;

            public Fail(OperationStatus reason) {
                this.reason = reason;
            }

            @Override
            public <R> R fold(final Function<OperationStatus, R> onFail, final Function<AddonPropertyIterable, R> onSuccess) {
                return onFail.apply(reason);
            }
        }

        class Success implements GetAllServiceResult {
            private final AddonPropertyIterable propertyIterable;

            public Success(AddonPropertyIterable propertyIterable) {
                this.propertyIterable = propertyIterable;
            }

            @Override
            public <R> R fold(final Function<OperationStatus, R> onFail, final Function<AddonPropertyIterable, R> onSuccess) {
                return onSuccess.apply(propertyIterable);
            }
        }
    }

    @Immutable
    class PutOperationStatus implements OperationStatus {
        private final OperationStatus base;
        private final AddonProperty property;

        public PutOperationStatus(final OperationStatus base, final AddonProperty property) {
            this.base = base;
            this.property = property;
        }

        @Override
        public int getHttpStatusCode() {
            return base.getHttpStatusCode();
        }

        @Override
        public String message(final I18nResolver resolver) {
            return base.message(resolver);
        }

        @Override
        public String getKey() {
            return base.getKey();
        }

        public OperationStatus getBase() {
            return base;
        }

        public AddonProperty getProperty() {
            return property;
        }
    }

    interface PutServiceResult<T> extends FoldableServiceResult<T, PutOperationStatus> {
        class PreconditionFail<T> implements PutServiceResult<T> {
            private final T object;

            public PreconditionFail(T object) {
                this.object = object;
            }

            @Override
            public <R> R fold(final Function<T, R> onPreconditionFailed, final Function<OperationStatus, R> onFail, final Function<PutOperationStatus, R> onSuccess) {
                return onPreconditionFailed.apply(object);
            }
        }

        class Fail<T> implements PutServiceResult<T> {
            private final OperationStatus reason;

            public Fail(OperationStatus reason) {
                this.reason = reason;
            }

            @Override
            public <R> R fold(final Function<T, R> onPreconditionFailed, final Function<OperationStatus, R> onFail, final Function<PutOperationStatus, R> onSuccess) {
                return onFail.apply(reason);
            }
        }

        class Success<T> implements PutServiceResult<T> {
            private final PutOperationStatus result;

            public Success(PutOperationStatus result) {
                this.result = result;
            }

            @Override
            public <R> R fold(final Function<T, R> onPreconditionFailed, final Function<OperationStatus, R> onFail, final Function<PutOperationStatus, R> onSuccess) {
                return onSuccess.apply(result);
            }
        }
    }

    interface DeleteServiceResult<T> extends FoldableServiceResult<T, OperationStatus> {
        class PreconditionFail<T> implements DeleteServiceResult<T> {
            private final T object;

            public PreconditionFail(T object) {
                this.object = object;
            }

            @Override
            public <R> R fold(final Function<T, R> onPreconditionFailed, final Function<OperationStatus, R> onFail, final Function<OperationStatus, R> onSuccess) {
                return onPreconditionFailed.apply(object);
            }
        }

        class Fail<T> implements DeleteServiceResult<T> {
            private final OperationStatus reason;

            public Fail(OperationStatus reason) {
                this.reason = reason;
            }

            @Override
            public <R> R fold(final Function<T, R> onPreconditionFailed, final Function<OperationStatus, R> onFail, final Function<OperationStatus, R> onSuccess) {
                return onFail.apply(reason);
            }
        }

        class Success<T> implements DeleteServiceResult<T> {
            private final OperationStatus reason;

            public Success(OperationStatus reason) {
                this.reason = reason;
            }

            @Override
            public <R> R fold(final Function<T, R> onPreconditionFailed, final Function<OperationStatus, R> onFail, final Function<OperationStatus, R> onSuccess) {
                return onSuccess.apply(reason);
            }
        }
    }
}
