package com.atlassian.plugin.connect.api.util;

import java.util.Optional;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class JsonCommonTest
{
    @Test
    public void stringInQuotesIsReturnedInQuotesWhenUsingToString()
    {
        assertThat(JsonCommon.parseStringToJson("\"aa\\\"a\"").get().toString(), equalTo("\"aa\\\"a\""));
    }

    @Test
    public void stringMustBeInQuotes()
    {
        assertThat(JsonCommon.parseStringToJson("string"), equalTo(Optional.empty()));
    }

    @Test
    public void parsingInvalidJsonReturnsEmpty()
    {
        assertThat(JsonCommon.parseStringToJson("{d: asdsda"), equalTo(Optional.empty()));
    }
}

