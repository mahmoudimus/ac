package it.jira.condition;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class ConditionUnderTest {
    private final String name;
    private final Map<String, String> parameters;

    public static ConditionUnderTest condition(String name) {
        return new ConditionUnderTest(name, Collections.emptyMap());
    }

    public ConditionUnderTest withParam(String key, String value) {
        return new ConditionUnderTest(name, ImmutableMap.<String, String>builder().putAll(parameters).put(key, value).build());
    }

    private ConditionUnderTest(String name, Map<String, String> parameters) {
        this.name = Preconditions.checkNotNull(name);
        this.parameters = ImmutableMap.copyOf(parameters);
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConditionUnderTest that = (ConditionUnderTest) o;

        return Objects.equals(this.getName(), that.getName()) &&
                Objects.equals(this.getParameters(), that.getParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getParameters());
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("name", getName())
                .add("parameters", getParameters())
                .toString();
    }
}
