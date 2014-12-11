package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import org.apache.commons.httpclient.HttpStatus;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
public interface AddOnPropertyService
{
    /**

     TODO: add user?
     */
    enum OperationStatus
    {
        PROPERTY_UPDATED(HttpStatus.SC_OK),
        PROPERTY_CREATED(HttpStatus.SC_CREATED),
        KEY_TOO_LONG(HttpStatus.SC_BAD_REQUEST),
        INVALID_ADDON(HttpStatus.SC_FORBIDDEN),
        ADDON_NOT_FOUND(HttpStatus.SC_NOT_FOUND),
        MAXIMUM_PROPERTIES_EXCEEDED(HttpStatus.SC_CONFLICT),
        PROPERTY_NOT_FOUND(HttpStatus.SC_NOT_FOUND),
        VALUE_TOO_BIG(HttpStatus.SC_FORBIDDEN);

        private final int httpStatusCode;

        private OperationStatus(int httpStatusCode) { this.httpStatusCode = httpStatusCode; }

        public int getHttpStatusCode()
        {
            return httpStatusCode;
        }
    }

    class ValidationErrorWithReason
    {
        final OperationStatus error;
        final String reason;

        public ValidationErrorWithReason(final OperationStatus error, final String reason) {
            this.error = error;
            this.reason = reason;
        }

        public OperationStatus getError()
        {
            return error;
        }

        public String getReason()
        {
            return reason;
        }
    }

    Either<AddOnProperty,Iterable<ValidationErrorWithReason>> getPropertyValue(@Nonnull String addonKey,@Nonnull String propertyKey);

    OperationStatus setPropertyValue(@Nonnull String addonKey, String propertyKey, String value);

    class ValidationResult<T>
    {
        private final Either<T,List<ValidationErrorWithReason>> result;

        public ValidationResult(final Either<T, List<ValidationErrorWithReason>> result) {
            this.result = result;
            }

        public boolean isValid()
        {
            return result.isRight() && result.right().get().isEmpty();
        }

        public Iterable<ValidationErrorWithReason> getErrorCollection()
        {
            return result.isRight() ? result.right().get() : Collections.EMPTY_LIST;
        }

        public ValidationErrorWithReason getWorstError()
        {
            return isValid()? null: result.right().get().get(0) ;
        }

        public Option<T> getValue()
        {
            return result.left().toOption();
        }
    }


}
