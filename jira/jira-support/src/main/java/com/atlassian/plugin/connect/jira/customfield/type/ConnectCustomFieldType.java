package com.atlassian.plugin.connect.jira.customfield.type;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public abstract class ConnectCustomFieldType<T> implements CustomFieldType<T, T>
{
    protected final Gson gson = new Gson();
    protected final JsonParser jsonParser = new JsonParser();

    private final JsonPropertyStore jsonPropertyStore;
    private CustomFieldTypeModuleDescriptor descriptor;

    public ConnectCustomFieldType(final JsonPropertyStore jsonPropertyStore)
    {
        this.jsonPropertyStore = jsonPropertyStore;
    }

    protected abstract String toJson(T value);

    protected abstract T fromJson(String json);

    protected abstract T viewToValue(String viewValue);

    protected abstract String valueToEditMode(T value);

    protected abstract ErrorCollection validateInput(@Nullable String value);

    protected abstract T getDefaultValue();

    protected abstract Map<String, Object> getVelocityParameters(final Issue issue);

    @Override
    public final void init(final CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor)
    {
        this.descriptor = customFieldTypeModuleDescriptor;
    }

    @Override
    public final String getKey()
    {
        return descriptor.getCompleteKey();//.replace("com.atlassian.plugins.atlassian-connect-plugin:", "");
    }

    @Override
    public final String getName()
    {
        return descriptor.getName();
    }

    @Override
    public final String getDescription()
    {
        return descriptor.getDescription();
    }

    @Override
    public final CustomFieldTypeModuleDescriptor getDescriptor()
    {
        return descriptor;
    }

    @Override
    public final String getStringFromSingularObject(final T t)
    {
        return valueToEditMode(t);
    }

    @Override
    public final T getSingularObjectFromString(final String s) throws FieldValidationException
    {
        return fromJson(s);
    }

    @Override
    public final Set<Long> remove(final CustomField customField)
    {
        return Collections.emptySet(); // TODO: implement
    }

    @Override
    public final void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config)
    {
        String stringParam = getStringValueFromCustomFieldParams(relevantParams);
        ErrorCollection validationResult = validateInput(stringParam);
        errorCollectionToAddTo.addErrorCollection(validationResult);
    }

    @Override
    public final void createValue(final CustomField field, final Issue issue, @Nonnull final T value)
    {
        jsonPropertyStore.storeValue(getKey(), field.getId(), issue, toJson(value));
    }

    @Override
    public final void updateValue(final CustomField field, final Issue issue, final T value)
    {
        jsonPropertyStore.storeValue(getKey(), field.getId(), issue, toJson(value));
    }

    @Override
    public final T getValueFromCustomFieldParams(final CustomFieldParams parameters) throws FieldValidationException
    {
        return Optional.ofNullable(getStringValueFromCustomFieldParams(parameters))
                .map(this::viewToValue)
                .orElse(null);
    }

    @Override
    public final String getStringValueFromCustomFieldParams(final CustomFieldParams parameters)
    {
        return (String) parameters.getFirstValueForNullKey();
    }

    @Nullable
    @Override
    public final T getValueFromIssue(final CustomField field, final Issue issue)
    {
        String json = jsonPropertyStore.loadValue(getKey(), field.getId(), issue);
        return fromJson(json);
    }

    @Override
    public final T getDefaultValue(final FieldConfig fieldConfig)
    {
        return getDefaultValue();
    }

    @Override
    public final void setDefaultValue(final FieldConfig fieldConfig, final T value)
    {
        // we probably don't need this?
    }

    @Nullable
    @Override
    public final String getChangelogValue(final CustomField field, final T value)
    {
        return toJson(value);
    }

    @Nullable
    @Override
    public final String getChangelogString(final CustomField field, final T value)
    {
        return Objects.toString(value);
    }

    @Nonnull
    @Override
    public final Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        return ImmutableMap.<String, Object>builder()
                .put("issue", issue)
                .put("field", field)
                .putAll(getVelocityParameters(issue))
                .build();
    }

    @Nonnull
    @Override
    public final List<FieldConfigItemType> getConfigurationItemTypes()
    {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public final List<FieldIndexer> getRelatedIndexers(final CustomField customField)
    {
        return Collections.emptyList();
    }

    @Override
    public final boolean isRenderable()
    {
        return true;
    }

    @Override
    public final boolean valuesEqual(final T v1, final T v2)
    {

        return Objects.equals(jsonParser.parse(toJson(v1)), jsonParser.parse(toJson(v2)));
    }

    @Nullable
    @Override
    public String availableForBulkEdit(final BulkEditBean bulkEditBean)
    {
        return "not implemented yet";
    }
}
