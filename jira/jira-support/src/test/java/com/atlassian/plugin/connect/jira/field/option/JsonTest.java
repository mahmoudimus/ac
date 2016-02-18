package com.atlassian.plugin.connect.jira.field.option;

import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.connect.jira.util.Json;
import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.JsonNode;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
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

    @Test
    public void stringAsObjectIsQuoted()
    {
        assertThat(Json.toJsonNode("jsonString").toString(), equalTo("\"jsonString\""));
    }

    @Test
    public void mapCanBeTransformedToJson()
    {
        Map<String, Object> input = ImmutableMap.of(
                "numVal", 4,
                "strVal", "bazinga",
                "nested", ImmutableMap.of("a", 1));

        JsonNode json = Json.toJsonNode(input);
        assertThat(json.get("numVal").asInt(), equalTo(4));
        assertThat(json.get("strVal").asText(), equalTo("bazinga"));
        assertThat(json.get("nested").get("a").asInt(), equalTo(1));
    }

    @Test
    public void numbersAreHandledUniformly()
    {
        assertEquals(Json.toJsonNode(4), Json.parse("4").get());
    }
}
