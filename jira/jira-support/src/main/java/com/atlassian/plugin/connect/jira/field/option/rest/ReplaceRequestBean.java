package com.atlassian.plugin.connect.jira.field.option.rest;

import java.util.Objects;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect
public final class ReplaceRequestBean
{
    private final Integer from;
    private final Integer to;

    @JsonCreator
    public ReplaceRequestBean(@JsonProperty ("from") Integer from, @JsonProperty ("to") Integer to)
    {
        this.from = from;
        this.to = to;
    }

    public Integer getFrom()
    {
        return from;
    }

    public Integer getTo()
    {
        return to;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ReplaceRequestBean that = (ReplaceRequestBean) o;

        return Objects.equals(this.getFrom(), that.getFrom()) &&
                Objects.equals(this.getTo(), that.getTo());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getFrom(), getTo());
    }

    @Override
    public String toString()
    {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("from", getFrom())
                .add("to", getTo())
                .toString();
    }
}
