package com.atlassian.plugin.connect.jira.field.option.rest;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Objects;

@JsonAutoDetect
public class ConnectFieldOptionScopeBean {
    private final Long projectId;

    @JsonCreator
    public ConnectFieldOptionScopeBean(@JsonProperty("projectId") Long projectId) {
        this.projectId = projectId;
    }

    public Long getProjectId() {
        return projectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConnectFieldOptionScopeBean that = (ConnectFieldOptionScopeBean) o;

        return Objects.equals(this.getProjectId(), that.getProjectId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProjectId());
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("projectId", getProjectId())
                .toString();
    }
}
