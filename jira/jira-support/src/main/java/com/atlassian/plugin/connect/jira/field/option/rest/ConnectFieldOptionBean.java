package com.atlassian.plugin.connect.jira.field.option.rest;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Objects;

@JsonAutoDetect
public final class ConnectFieldOptionBean {
    private final Integer id;
    private final Object value;
    private final ConnectFieldOptionScopeBean scope;

    @JsonCreator
    public ConnectFieldOptionBean(@JsonProperty("id") Integer id, @JsonProperty("value") Object value, @JsonProperty("scope") ConnectFieldOptionScopeBean scope) {
        this.id = id;
        this.value = value;
        this.scope = scope;
    }

    public Integer getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }

    public ConnectFieldOptionScopeBean getScope() {
        return scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConnectFieldOptionBean that = (ConnectFieldOptionBean) o;

        return Objects.equals(this.getId(), that.getId()) &&
                Objects.equals(this.getValue(), that.getValue()) &&
                Objects.equals(this.getScope(), that.getScope());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getValue(), getScope());
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("id", getId())
                .add("value", getValue())
                .add("scope", getScope())
                .toString();
    }
}
