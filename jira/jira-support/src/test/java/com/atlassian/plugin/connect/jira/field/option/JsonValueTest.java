package com.atlassian.plugin.connect.jira.field.option;

import java.util.Optional;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class JsonValueTest
{
    @Test
    public void stringJsonValueIsEscaped() {
        assertThat(JsonValue.parse("aa\"a").get().toJson(), equalTo("\"aa\\\"a\""));
    }

    @Test
    public void parsingInvalidJsonReturnsEmpty() {
        assertThat(JsonValue.parse("{d: asdsda"), equalTo(Optional.empty()));
    }
}
