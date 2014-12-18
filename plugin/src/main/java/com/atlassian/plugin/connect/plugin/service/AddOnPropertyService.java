package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import org.apache.commons.httpclient.HttpStatus;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * This service is used to add, remove, list and update add-on properties.
 * Checks permissions and constraints on input before executing an action.
 *
 * @since TODO: fill in the proper version before merge
 */
public interface AddOnPropertyService
{
    enum ServiceResult
    {
        PROPERTY_UPDATED(HttpStatus.SC_OK),
        PROPERTY_CREATED(HttpStatus.SC_CREATED),
        KEY_TOO_LONG(HttpStatus.SC_BAD_REQUEST),
        INVALID_ADD_ON(HttpStatus.SC_FORBIDDEN),
        ADD_ON_NOT_FOUND(HttpStatus.SC_NOT_FOUND),
        MAXIMUM_PROPERTIES_EXCEEDED(HttpStatus.SC_CONFLICT),
        PROPERTY_NOT_FOUND(HttpStatus.SC_NOT_FOUND),
        VALUE_TOO_BIG(HttpStatus.SC_FORBIDDEN),
        ACCESS_FORBIDDEN(HttpStatus.SC_FORBIDDEN),
        INVALID_FORMAT(HttpStatus.SC_BAD_REQUEST);

        private final int httpStatusCode;

        private ServiceResult(int httpStatusCode)
        {
            this.httpStatusCode = httpStatusCode;
        }

        public int getHttpStatusCode()
        {
            return httpStatusCode;
        }
    }

    class ServiceResultWithReason
    {
        final ServiceResult result;
        final String reason;

        public ServiceResultWithReason(final ServiceResult result, final String reason)
        {
            this.result = result;
            this.reason = reason;
        }

        public ServiceResult getResult()
        {
            return result;
        }

        public String getReason()
        {
            return reason;
        }
    }

    Either<ServiceResultWithReason, AddOnProperty> getPropertyValue(String sourcePluginKey, @Nonnull String addOnKey,@Nonnull String propertyKey);

    ServiceResult setPropertyValue(String sourcePluginKey, @Nonnull String addOnKey, String propertyKey, String value);

    class ValidationResult<T>
    {
        private final Either<List<ServiceResultWithReason>,T> result;

        public ValidationResult(Either<List<ServiceResultWithReason>,T> result)
        {
            this.result = result;
        }

        public boolean isValid()
        {
            return result.isRight();
        }

        public Iterable<ServiceResultWithReason> getErrorCollection()
        {
            return result.isLeft() ? result.left().get() : Collections.<ServiceResultWithReason>emptyList();
        }

        public Option<T> getValue()
        {
            return result.right().toOption();
        }

        public static <T> ValidationResult<T> fromValue(T value){
            return new ValidationResult<T>(Either.<List<ServiceResultWithReason>,T>right(value));
        }
        public static <T> ValidationResult<T> fromError(List<ServiceResultWithReason> errorCollection){
            return new ValidationResult<T>(Either.<List<ServiceResultWithReason>,T>left(errorCollection));
        }
    }


}
