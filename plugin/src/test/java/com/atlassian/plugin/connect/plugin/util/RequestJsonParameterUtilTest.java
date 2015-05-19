package com.atlassian.plugin.connect.plugin.util;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.iframe.context.InvalidContextParameterException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.plugin.util.RequestJsonParameterUtil.CONTEXT_PARAMETER_KEY;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class RequestJsonParameterUtilTest
{
    RequestJsonParameterUtil extractor = new RequestJsonParameterUtil();

    @Test
    public void doesNotModifyParamsIfContextParamNotProvided()
    {
        final Map<String, String[]> requestParams = ImmutableMap.of(
                "project.id", new String[]{"10", "20"},
                "project.key", new String[]{"myKey"},
                "other.blah", new String[]{"stuff"});

        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);

        assertThat(processedParams.entrySet(), is(requestParams.entrySet()));
    }

    @Test
    public void extractsContextFromParamAndAddsToParamMap()
    {
        String[] contextJsonStr = new String[]{"{\"project\":{\"id\":10100}}"};

        Map<String, String[]> requestParams = ImmutableMap.of(CONTEXT_PARAMETER_KEY, contextJsonStr, "someparam", new String[]{"somevalue"});

        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);
        assertThat(processedParams, hasEntry("project.id", new String[]{"10100"}));
        assertThat(processedParams, hasEntry("someparam", new String[]{"somevalue"}));
        assertThat(processedParams, not(hasKey((Object) CONTEXT_PARAMETER_KEY)));
        assertThat(processedParams.size(), is(2));
    }

    @Test(expected = InvalidContextParameterException.class)
    public void throwsErrorWhenFailToParseJson()
    {
        extractor.tryExtractContextFromJson(ImmutableMap.of(CONTEXT_PARAMETER_KEY, new String[]{"mary had a little lamb"}));
    }

    @Test(expected = InvalidContextParameterException.class)
    public void throwsErrorWhenFailToParseJsonDueToNullValue()
    {
        extractor.tryExtractContextFromJson(ImmutableMap.of(CONTEXT_PARAMETER_KEY, new String[]{"{\"project\":{\"key\":}}"}));
    }

    @Test
    public void emptyJsonValueMapsToSingleArrayWithEmptyString()
    {
        String[] contextJsonStr = new String[]{"{\"project\":{\"key\": \"\"}}"};

        Map<String, String[]> requestParams = ImmutableMap.of(CONTEXT_PARAMETER_KEY, contextJsonStr);
        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);
        assertThat(processedParams.get("project.key"), is(new String[]{""}));
    }

    @Test
    public void contextJsonParamOverwritesUrlParam()
    {
        String[] contextJsonStr = new String[]{"{\"project\":{\"id\":10100}}"};

        Map<String, String[]> requestParams = ImmutableMap.of(CONTEXT_PARAMETER_KEY, contextJsonStr, "project.id",
                new String[]{"2222", "3333"});
        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);
        assertThat(processedParams.entrySet(), hasSize(1));
        assertThat(processedParams.get("project.id"), is(new String[]{"10100"}));
    }

    @Test
    public void jsonArraysConvertToStringArrayValues()
    {
        String[] contextJsonStr = new String[]{"{\"project\":{\"ids\": [10100, 500]}}"};

        Map<String, String[]> requestParams = ImmutableMap.of(CONTEXT_PARAMETER_KEY, contextJsonStr);
        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);
        assertThat(processedParams.entrySet(), hasSize(1));
        assertThat(processedParams.get("project.ids"), is(new String[]{"10100", "500"}));
    }

    @Test
    public void copesWithNullValuesInParameters()
    {
        String[] contextJsonStr = new String[]{"{\"project\":{\"id\":10100}}"};

        final HashMap<String, String[]> requestParams = Maps.newHashMap();
        requestParams.put("project.id", null);
        requestParams.put(CONTEXT_PARAMETER_KEY, contextJsonStr);

        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);
        assertThat(processedParams.entrySet(), hasSize(1));
        assertThat(processedParams.get("project.id"), is(new String[]{"10100"}));
    }

    @Test
    public void copesWithExplicitNullValuesAtTopLevel()
    {
        ImmutableMap<String, String[]> requestParams =
                ImmutableMap.of(CONTEXT_PARAMETER_KEY, new String[] { "{\"project\":null}" });
        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);
        assertThat(processedParams.entrySet(), hasSize(1));
        assertThat(processedParams.get("project"), is(new String[] {null}));
    }


    @Test
    public void upperCaseUrlParamsNotOverriddenByJson()
    {
        String[] contextJsonStr = new String[]{"{\"project\":{\"id\":10100}}"};

        Map<String, String[]> requestParams = ImmutableMap.of(CONTEXT_PARAMETER_KEY, contextJsonStr, "PROJECT.ID",
                new String[]{"2222", "3333"});
        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);
        assertThat(processedParams.entrySet(), hasSize(2));
        assertThat(processedParams.get("project.id"), is(new String[]{"10100"}));
        assertThat(processedParams.get("PROJECT.ID"), is(new String[]{"2222", "3333"}));
    }

    @Test
    public void upperCaseContextJsonParamDoesNotOverwriteLowerCaseUrlParam()
    {
        String[] contextJsonStr = new String[]{"{\"PROJECT\":{\"ID\":10100}}"};

        Map<String, String[]> requestParams = ImmutableMap.of(CONTEXT_PARAMETER_KEY, contextJsonStr, "project.id",
                new String[]{"2222", "3333"});
        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);
        assertThat(processedParams.entrySet(), hasSize(2));
        assertThat(processedParams.get("PROJECT.ID"), is(new String[]{"10100"}));
        assertThat(processedParams.get("project.id"), is(new String[]{"2222", "3333"}));
    }

    @Test(expected = InvalidContextParameterException.class)
    public void throwsWhenMultipleJsonParamsSupplied()
    {
        String[] contextJsonStr = new String[]{"{\"project\":{\"id\":10100}}", "{\"issue\":{\"key\":\"FOO\"}}"};

        extractor.tryExtractContextFromJson(ImmutableMap.of(CONTEXT_PARAMETER_KEY, contextJsonStr));
    }


    @Test
    public void nonUTFIsAccepted()
    {
        String[] contextJsonStr = new String[]{"{\"project\":{\"key\": \"foo\\ud800\\udc35\"}}"};

        Map<String, String[]> requestParams = ImmutableMap.of(CONTEXT_PARAMETER_KEY, contextJsonStr);
        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);
        assertThat(processedParams.get("project.key"), is(new String[]{"foo\ud800\udc35"}));
    }
}
