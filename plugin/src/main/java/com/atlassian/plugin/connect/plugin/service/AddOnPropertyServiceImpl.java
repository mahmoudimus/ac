package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.fugue.Option;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
@Component
public class AddOnPropertyServiceImpl implements AddOnPropertyService
{
    public static final int MAXIMUM_VALUE_LENGTH = 255;

    private final AddOnPropertyStore store;

    @Autowired
    public AddOnPropertyServiceImpl(final AddOnPropertyStore store) {this.store = checkNotNull(store);}

    @Override
    public ValidationResult<GetPropertyInput> validateGetPropertyValue(@Nonnull final String addonKey, @Nonnull final String propertyKey)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        if (store.existsProperty(addonKey, propertyKey))
        {
            errorCollection.addErrorMessage("No value with such key found");
            return new ValidationResult<GetPropertyInput>(Option.<GetPropertyInput>none(), errorCollection);
        }
        return new ValidationResult<GetPropertyInput>(Option.option(new GetPropertyInput(addonKey, propertyKey)),errorCollection);
    }

    @Override
    public AddOnProperty getPropertyValue(ValidationResult<GetPropertyInput> validationResult)
    {
        checkNotNull(validationResult);
        checkArgument(validationResult.isValid());

        GetPropertyInput input = validationResult.getValue().getOrNull();
        return store.getPropertyValue(input.addonKey, input.propertyKey);
    }

    @Override
    public void setPropertyValue(@Nonnull final String addonKey, final String propertyKey, final String value)
    {
        store.setPropertyValue(addonKey, propertyKey, value);
    }
}
