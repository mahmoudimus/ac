package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.fugue.Option;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;

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
    ValidationResult<GetPropertyInput> validateGetPropertyValue(@Nonnull String addonKey,@Nonnull String propertyKey);

    AddOnProperty getPropertyValue(ValidationResult<GetPropertyInput> validationResult);

    void setPropertyValue(@Nonnull String addonKey, String propertyKey, String value); //TODO: Result instead of void

    class ValidationResult<T>
    {
        private final Option<T> value;
        private final ErrorCollection errorCollection;

        public ValidationResult(final Option<T> value, final ErrorCollection errorCollection) {
            this.value = value;
            this.errorCollection = errorCollection;}

        public boolean isValid()
        {
            return !errorCollection.hasAnyErrors();
        }

        public ErrorCollection getErrorCollection()
        {
            return errorCollection;
        }

        public Option<T> getValue()
        {
            return value;
        }
    }

    class GetPropertyInput
    {
        final String addonKey;
        final String propertyKey;

        public GetPropertyInput(final String addonKey, final String propertyKey)
        {
            this.addonKey = addonKey;
            this.propertyKey = propertyKey;
        }

        public String getAddonKey()
        {
            return addonKey;
        }

        public String getPropertyKey()
        {
            return propertyKey;
        }
    }
}
