package com.atlassian.plugin.connect.jira.field.option;

import java.util.Objects;

import com.google.common.base.Preconditions;
import org.codehaus.jackson.JsonNode;

public final class RemoteFieldOption
{
    private final Integer id;
    private final JsonNode value;

    public static RemoteFieldOption option(Integer id, JsonNode value) {
        return new RemoteFieldOption(id, value);
    }

    private RemoteFieldOption(Integer id, JsonNode value)
    {
        this.id = Preconditions.checkNotNull(id);
        this.value = Preconditions.checkNotNull(value);
    }

    public Integer getId()
    {
        return id;
    }

    public JsonNode getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        RemoteFieldOption that = (RemoteFieldOption) o;

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
                .add("value", getValue())
                .toString();
    }
}
