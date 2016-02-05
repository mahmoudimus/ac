package com.atlassian.plugin.connect.jira.field.option;

import java.util.Objects;

import com.google.common.base.Preconditions;

public final class AvailableOption
{
    private final Integer id;
    private final JsonValue value;

    public static AvailableOption option(Integer id, JsonValue value) {
        return new AvailableOption(id, value);
    }

    private AvailableOption(Integer id, JsonValue value)
    {
        this.id = Preconditions.checkNotNull(id);
        this.value = Preconditions.checkNotNull(value);
    }

    public Integer getId()
    {
        return id;
    }

    public JsonValue getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        AvailableOption that = (AvailableOption) o;

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
