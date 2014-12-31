package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.sal.api.user.UserProfile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This service is used to add, remove, list and update add-on properties.
 * Checks permissions and constraints on input before executing an action.
 *
 * @since TODO: fill in the proper version before merge
 */
public interface AddOnPropertyService
{
    /**
     * Gets a property from the add-on store.
     * <p>
     *     This method checks parameter validity and tries to get a property for an add-on.
     * </p>
     * @return either error result or add-on property.
     **/
    Either<ServiceResult, AddOnProperty> getPropertyValue(@Nullable UserProfile user, @Nullable String sourcePluginKey, @Nonnull String addOnKey,@Nonnull String propertyKey);

    /**
     * Sets a property from the add-on store.
     * <p>
     *     This method checks parameter validity and tries to set a property for an add-on.
     * </p>
     * @return either error result or add-on property.
     **/
    ServiceResult setPropertyValue(@Nullable UserProfile user, @Nullable String sourcePluginKey, @Nonnull String addOnKey, @Nonnull String propertyKey, @Nonnull String value);

    /**
     * Deletes a property from the add-on store.
     * <p>
     *     This method checks parameter validity and tries to delete a property for an add-on.
     * </p>
     * @return either error result or add-on property.
     **/
    ServiceResult deletePropertyValue(@Nullable UserProfile user, @Nullable String sourcePluginKey, @Nonnull String addOnKey, @Nonnull String propertyKey);

    interface ServiceResult
    {
        public int getHttpStatusCode();
        public String message();
    }

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
