package com.atlassian.plugin.connect.plugin.iframe.context;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextJsonExtractor.CONTEXT_PARAMETER_KEY;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class ModuleContextJsonExtractorTest
{
    ModuleContextJsonExtractor extractor = new ModuleContextJsonExtractor();

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

    @Test
    public void contextJsonParamOverwritesUrlParam()
    {
        String[] contextJsonStr = new String[]{"{\"project\":{\"id\":10100}}"};

        Map<String, String[]> requestParams = ImmutableMap.of(CONTEXT_PARAMETER_KEY, contextJsonStr, "project.id", new String[]{"2222"});
        final Map<String, String[]> processedParams = extractor.tryExtractContextFromJson(requestParams);
        assertThat(processedParams.entrySet(), hasSize(1));
        assertThat(processedParams.get("project.id"), is(new String[]{"10100"}));

    }
}
