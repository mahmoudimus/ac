package com.atlassian.plugin.connect.jira.field.option.rest;

import java.util.Objects;
import java.util.Optional;

import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOption;
import com.atlassian.plugin.connect.jira.field.option.Json;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonRawValue;

public final class ConnectFieldOptionBean
{
    @JsonProperty
    private final Integer id;
    @JsonRawValue
    @JsonProperty
    private final String value;

    @JsonCreator
    public ConnectFieldOptionBean(@JsonProperty ("id") Integer id, @JsonProperty ("json") String value)
    {
        this.id = id;
        this.value = value;
    }

    public Integer getId()
    {
        return id;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ConnectFieldOptionBean that = (ConnectFieldOptionBean) o;

        return Objects.equals(this.getId(), that.getId()) &&
                Objects.equals(this.getValue(), that.getValue());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getId(), getValue());
    }

    @Override
    public String toString()
    {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("id", getId())
                .add("json", getValue())
                .toString();
    }
}
