package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyIterable;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * This service is used to add, remove, list and update add-on properties.
 * Checks permissions and constraints on input before executing an action.
 *
 * @since TODO: fill in the proper version before merge
 */
public interface AddOnPropertyService
{
    /**
     * Gets a property from the add-on store. <p> This method checks parameter validity and tries to get a property for
     * an add-on. </p>
     *
     * @return either error result or add-on property.
     */
    Either<ServiceResult, AddOnProperty> getPropertyValue(@Nullable UserProfile user, @Nullable String sourcePluginKey, @Nonnull String addOnKey, @Nonnull String propertyKey);

    /**
     * Sets a property from the add-on store. <p> This method checks parameter validity and tries to set a property for
     * an add-on. </p>
     *
     * @return FoldableServiceResult which calls one of three callbacks: onPreconditionFailed, OnFailed or OnSuccess
     */
    <X> PutServiceResult<X> setPropertyValueIfConditionSatisfied(@Nullable UserProfile user, @Nullable String sourcePluginKey, @Nonnull String addOnKey, @Nonnull String propertyKey, @Nonnull String value, @Nonnull final Function<Option<AddOnProperty>, ServiceConditionResult<X>> testFunction);

    /**
     * Deletes a property from the add-on store. <p> This method checks parameter validity and tries to delete a
     * property for an add-on. </p>
     *
     * @return FoldableServiceResult which calls one of three callbacks: onPreconditionFailed, OnFailed or OnSuccess
     */
    <X> DeleteServiceResult<X> deletePropertyValueIfConditionSatisfied(@Nullable UserProfile user, @Nullable String sourcePluginKey, @Nonnull String addOnKey, @Nonnull String propertyKey, @Nonnull final Function<Option<AddOnProperty>, ServiceConditionResult<X>> testFunction);

    /**
     * Returns a list of all properties for a given add-on. <p> This method checks parameter validity and list all
     * properties belonging to an add-on. </p>
     *
     * @return either error result or list of add-on properties.
     */
    Either<ServiceResult, AddOnPropertyIterable> getAddOnProperties(@Nullable UserProfile user, @Nullable String sourcePluginKey, @Nonnull String addOnKey);

    interface ServiceResult
    {
        public int getHttpStatusCode();

        public String message(I18nResolver resolver);

        public String getKey();
    }

    @Immutable
    class ServicePutResult implements ServiceResult
    {
        private final ServiceResult base;
        private final AddOnProperty property;

        public ServicePutResult(final ServiceResult base, final AddOnProperty property)
        {
            this.base = base;
            this.property = property;
        }

        @Override
        public int getHttpStatusCode()
        {
            return base.getHttpStatusCode();
        }

        @Override
        public String message(final I18nResolver resolver)
        {
            return base.message(resolver);
        }

        @Override
        public String getKey()
        {
            return base.getKey();
        }

        public ServiceResult getBase()
        {
            return base;
        }

        public AddOnProperty getProperty()
        {
            return property;
        }
    }

    @Immutable
    class ServiceConditionResult<X>
    {
        private final Option<X> object;
        private final boolean successful;

        private ServiceConditionResult(Option<X> object, boolean isSuccessful)
        {
            this.object = object;
            this.successful = isSuccessful;
        }

        public boolean isSuccessful()
        {
            return successful;
        }

        public Option<X> getObject()
        {
            return object;
        }

        public static <X> ServiceConditionResult<X> SUCCESS()
        {
            return new ServiceConditionResult<X>(null, true);
        }

        public static <X> ServiceConditionResult<X> FAILURE_WITH_OBJECT(X obj)
        {
            return new ServiceConditionResult<X>(Option.some(obj), false);
        }
    }

    interface PutServiceResult<X> extends FoldableServiceResult<X, ServicePutResult>
    {
        class PreconditionFail<X> implements PutServiceResult<X>
        {
            private final X object;

            public PreconditionFail(X object)
            {
                this.object = object;
            }

            @Override
            public <R> R fold(final Function<X, R> onPreconditionFailed, final Function<ServiceResult, R> onFail, final Function<ServicePutResult, R> onSuccess)
            {
                return onPreconditionFailed.apply(object);
            }
        }
        class Fail<X> implements PutServiceResult<X>
        {
            private final ServiceResult reason;

            public Fail(ServiceResult reason)
            {
                this.reason = reason;
            }

            @Override
            public <R> R fold(final Function<X, R> onPreconditionFailed, final Function<ServiceResult, R> onFail, final Function<ServicePutResult, R> onSuccess)
            {
                return onFail.apply(reason);
            }
        }
        class Success<X> implements PutServiceResult<X>
        {
            private final ServicePutResult result;

            public Success(ServicePutResult result)
            {
                this.result = result;
            }

            @Override
            public <R> R fold(final Function<X, R> onPreconditionFailed, final Function<ServiceResult, R> onFail, final Function<ServicePutResult, R> onSuccess)
            {
                return onSuccess.apply(result);
            }
        }
    }

    interface DeleteServiceResult<X> extends FoldableServiceResult<X, ServiceResult>
    {
        class PreconditionFail<X> implements DeleteServiceResult<X>
        {
            private final X object;

            public PreconditionFail(X object)
            {
                this.object = object;
            }

            @Override
            public <R> R fold(final Function<X, R> onPreconditionFailed, final Function<ServiceResult, R> onFail, final Function<ServiceResult, R> onSuccess)
            {
                return onPreconditionFailed.apply(object);
            }
        }
        class Fail<X> implements DeleteServiceResult<X>
        {
            private final ServiceResult reason;

            public Fail(ServiceResult reason)
            {
                this.reason = reason;
            }

            @Override
            public <R> R fold(final Function<X, R> onPreconditionFailed, final Function<ServiceResult, R> onFail, final Function<ServiceResult, R> onSuccess)
            {
                return onFail.apply(reason);
            }
        }
        class Success<X> implements DeleteServiceResult<X>
        {
            private final ServiceResult reason;

            public Success(ServiceResult reason)
            {
                this.reason = reason;
            }

            @Override
            public <R> R fold(final Function<X, R> onPreconditionFailed, final Function<ServiceResult, R> onFail, final Function<ServiceResult, R> onSuccess)
            {
                return onSuccess.apply(reason);
            }
        }
    }

    interface FoldableServiceResult<X, SRT extends ServiceResult>
    {
        public <R> R fold(Function<X, R> onPreconditionFailed,
                Function<ServiceResult,R> onFail,
                Function<SRT, R> onSuccess);
    }

    @Immutable
    class ValidationResult<T>
    {
        private final Either<ServiceResult,T> result;

        public ValidationResult(Either<ServiceResult,T> result)
        {
            this.result = result;
        }

        public boolean isValid()
        {
            return result.isRight();
        }

        public Option<ServiceResult> getError()
        {
            return result.left().toOption();
        }

        public Option<T> getValue()
        {
            return result.right().toOption();
        }

        public static <T> ValidationResult<T> fromValue(T value)
        {
            return new ValidationResult<T>(Either.<ServiceResult,T>right(value));
        }
        public static <T> ValidationResult<T> fromError(ServiceResult error)
        {
            return new ValidationResult<T>(Either.<ServiceResult,T>left(error));
        }
    }
}
