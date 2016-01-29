package com.atlassian.plugin.connect.jira.customfield.type;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TextConnectCustomFieldType extends ConnectCustomFieldType<String>
{
    @Autowired
    public TextConnectCustomFieldType(final JsonPropertyStore jsonPropertyStore)
    {
        super(jsonPropertyStore);
    }

    @Override
    protected String toJson(final String value)
    {
        return gson.toJson(value, String.class);
    }

    @Override
    protected String fromJson(final String json)
    {
        return json == null? null : json.substring(1, json.length() - 1);
    }

    @Override
    protected String viewToValue(final String viewValue)
    {
        return viewValue;
    }

    @Override
    protected String valueToEditMode(final String value)
    {
        return value;
    }

    @Override
    protected ErrorCollection validateInput(@Nullable final String value)
    {
        return ErrorCollections.empty();
    }

    @Override
    protected String getDefaultValue()
    {
        return "";
    }

    @Override
    protected Map<String, Object> getVelocityParameters(final Issue issue)
    {
        return Collections.emptyMap();
    }
}
