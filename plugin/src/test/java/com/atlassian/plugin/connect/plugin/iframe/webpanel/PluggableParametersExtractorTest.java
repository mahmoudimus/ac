package com.atlassian.plugin.connect.plugin.iframe.webpanel;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.iframe.context.HashMapModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.module.ConnectContextVariablesExtractorModuleDescriptor;
import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static com.atlassian.plugin.connect.test.util.UnitTestMatchers.predicateThatWillMatch;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public final class PluggableParametersExtractorTest
{
    private final static Map<String, Object> CONTEXT = ImmutableMap.<String, Object>of(
            "a", 1,
            "b", 2
    );

    private final static ModuleContextParameters LOCAL_PARAMS = moduleParamsFromMap(ImmutableMap.of(
            "a", "1",
            "b", "2"
    ));

    @Mock
    private WebFragmentModuleContextExtractor connectModuleContextExtractor;
    @Mock
    private PluginAccessor pluginAccessor;

    private PluggableParametersExtractor extractor;

    @Before
    public void setUp() throws Exception
    {
        extractor = new PluggableParametersExtractor(connectModuleContextExtractor, pluginAccessor);
        when(connectModuleContextExtractor.extractParameters(CONTEXT)).thenReturn(LOCAL_PARAMS);
    }

    @Test
    public void extractorReturnsWhatConnectReturnsWhenThereAreNoExtractorsFromPlugins()
    {
        assertThat(extractor.extractParameters(CONTEXT), equalTo(LOCAL_PARAMS));
    }

    @Test
    public void extractorAddsStuffFromAllThePluginsToTheLocalConnectParameters()
    {
        when(connectModuleContextExtractor.extractParameters(CONTEXT)).thenReturn(LOCAL_PARAMS);
        when(pluginAccessor.getModules(argThat(predicateThatWillMatch(new ConnectContextVariablesExtractorModuleDescriptor(mock(ModuleFactory.class)))))).thenReturn(ImmutableList.of(
                extractorReturning(ImmutableMap.of("q", "q")), extractorReturning(ImmutableMap.of("r", "r", "t", "t"))));

        assertThat(extractor.extractParameters(CONTEXT), equalTo(
                moduleParamsFromMap(ImmutableMap.<String, String>builder().putAll(LOCAL_PARAMS).put("q", "q").put("r", "r").put("t", "t").build())
        ));
    }

    @Test
    public void extractorKeepsCalmsAndCarriesOnWhenThereIsAnExceptionInAnyPlugin()
    {
        when(connectModuleContextExtractor.extractParameters(CONTEXT)).thenReturn(LOCAL_PARAMS);
        when(pluginAccessor.getModules(argThat(predicateThatWillMatch(new ConnectContextVariablesExtractorModuleDescriptor(mock(ModuleFactory.class)))))).thenReturn(Collections.<ContextParametersExtractor>singletonList(new ContextParametersExtractor() {
            @Override
            public Map<String, String> extractParameters(final Map<String, ? extends Object> context)
            {
                throw new RuntimeException("Geronimo!");
            }
        }));

        assertThat(extractor.extractParameters(CONTEXT), equalTo(LOCAL_PARAMS));
    }

    private ContextParametersExtractor extractorReturning(final ImmutableMap<String, String> parameters)
    {
        return new ContextParametersExtractor() {
            @Override
            public Map<String, String> extractParameters(final Map<String, ? extends Object> context)
            {
                return parameters;
            }
        };
    }

    private static ModuleContextParameters moduleParamsFromMap(Map<String, String> map)
    {
        HashMapModuleContextParameters result = new HashMapModuleContextParameters();
        result.putAll(map);
        return result;
    }

}
