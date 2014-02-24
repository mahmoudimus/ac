package com.atlassian.plugin.connect.plugin.iframe.context;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextJsonExtractor.CONTEXT_PARAMETER_KEY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class ModuleContextJsonExtractorTest
{
    ModuleContextJsonExtractor extractor = new ModuleContextJsonExtractor();

    @Test
    public void shouldConvertToNestedForm() //throws MalformedRequestException, UnauthorisedException, ResourceNotFoundException
    {
        final Map<String, String[]> requestParams = ImmutableMap.<String, String[]>of(
                "project.id", new String[]{"10"},
                "project.key", new String[]{"myKey"},
                "other.blah", new String[]{"stuff"});

        final Map<String, String[]> paramsInNestedForm = extractor.tryExtractContextFromJson(requestParams);
        assertThat(paramsInNestedForm.entrySet(), hasSize(2));

        final Object projectMapObj = paramsInNestedForm.get("project");
        assertThat(projectMapObj, is(notNullValue()));
        assertThat(projectMapObj, is(instanceOf(Map.class)));
        Map<?, ?> projectMap = (Map<?, ?>) projectMapObj;
        assertThat(projectMap.get("id"), is(equalTo((Object) "10")));
        assertThat(projectMap.get("key"), is(equalTo((Object) "myKey")));


        final Object otherMapObj = paramsInNestedForm.get("other");
        assertThat(otherMapObj, is(notNullValue()));
        assertThat(otherMapObj, is(instanceOf(Map.class)));
        Map<?, ?> otherMap = (Map<?, ?>) otherMapObj;
        assertThat(otherMap.get("blah"), is(equalTo((Object) "stuff")));
    }

    @Test
    public void doesNotModifyParamsIfContextParamNotProvided()
    {
        final Map<String, String[]> requestParams = ImmutableMap.of(
                "project.id", new String[]{"10"},
                "project.key", new String[]{"myKey"},
                "other.blah", new String[]{"stuff"});

        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);

        assertThat(processedParams.entrySet(), is(requestParams.entrySet()));
    }

    @Test
    public void shouldExtractContextAndAddToParamMap()
    {
        String[] contextJsonStr = new String[]{"{\"project\":{\"id\":10100}}"};

        Map<String, String[]> requestParams = ImmutableMap.of(CONTEXT_PARAMETER_KEY, contextJsonStr, "someparam", new String[]{"somevalue"});

        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);
        assertThat(processedParams, hasEntry("project.id", new String[]{"10100"}));
        assertThat(processedParams, hasEntry("someparam", new String[]{"somevalue"}));
        assertThat(processedParams, not(hasKey((Object) CONTEXT_PARAMETER_KEY)));
        assertThat(processedParams.size(), is(2));
    }

    // TODO: more tests. negative tests. corner cases
}
