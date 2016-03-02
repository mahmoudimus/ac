package com.atlassian.plugin.connect.plugin.web.context.condition;

import java.util.Map;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public final class InlineCondition {
    private final String conditionName;
    private final Map<String, String> params;

    public InlineCondition(String conditionName, Map<String, String> params) {
        this.conditionName = Preconditions.checkNotNull(conditionName);
        this.params = ImmutableMap.copyOf(params);
    }

    public String getConditionName() {
        return conditionName;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InlineCondition that = (InlineCondition) o;

        return Objects.equals(this.getConditionName(), that.getConditionName()) &&
                Objects.equals(this.getParams(), that.getParams());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConditionName(), getParams());
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("conditionName", getConditionName())
                .add("params", getParams())
                .toString();
    }
}
