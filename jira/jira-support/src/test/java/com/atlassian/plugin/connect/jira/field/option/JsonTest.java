package com.atlassian.plugin.connect.jira.field.option;

import java.util.Optional;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class JsonTest
{
    @Test
    public void stringInQuotesIsReturnedInQuotesWhenUsingToString()
    {
        assertThat(Json.parse("\"aa\\\"a\"").get().toString(), equalTo("\"aa\\\"a\""));
    }

    @Test
    public void stringMustBeInQuotes()
    {
        assertThat(Json.parse("string"), equalTo(Optional.empty()));
    }

    @Test
    public void parsingInvalidJsonReturnsEmpty()
    {
        assertThat(Json.parse("{d: asdsda"), equalTo(Optional.empty()));
    }
}
