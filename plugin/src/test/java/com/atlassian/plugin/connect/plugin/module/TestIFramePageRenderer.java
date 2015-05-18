package com.atlassian.plugin.connect.plugin.module;

import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.web.conditions.InvertedCondition;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.module.TestIFrameRenderer.createContext;
import static com.atlassian.plugin.connect.plugin.module.TestIFrameRenderer.emptyContext;
import static com.atlassian.plugin.connect.plugin.module.TestIFrameRenderer.emptyParams;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

public class TestIFramePageRenderer
{
    @Mock private TemplateRenderer templateRenderer;
    @Mock private IFrameRenderer iframeRenderer;
    @Mock private HostApplicationInfo hostApplicationInfo;

    private IFramePageRenderer iframePageRenderer;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        this.iframePageRenderer = new IFramePageRenderer(templateRenderer, iframeRenderer, hostApplicationInfo);
    }

    @Test
    public void testFailingCondition() throws Exception
    {
        StringWriter writer = new StringWriter();
        PageInfo pageInfo = new PageInfo("my-decorator", "-my-suffix", "my-title", new InvertedCondition(new AlwaysDisplayCondition()), Maps.<String, String>newHashMap());
        iframePageRenderer.renderPage(createContext("a.b", "my-path", "my-namespace"), pageInfo, "", emptyParams(), "jim", emptyContext(), writer);

        String renderedTemplate = getActualTemplateRendererPath();
        assertEquals("velocity/deprecated/iframe-page-accessdenied-my-suffix.vm", renderedTemplate);
    }

    @Test
    public void testSucceedingCondition() throws Exception
    {
        StringWriter writer = new StringWriter();
        PageInfo pageInfo = new PageInfo("my-decorator", "-my-suffix", "my-title", new AlwaysDisplayCondition(), Maps.<String, String>newHashMap());
        iframePageRenderer.renderPage(createContext("a.b", "my-path", "my-namespace"), pageInfo, "", emptyParams(), "jim", emptyContext(), writer);

        String renderedTemplate = getActualTemplateRendererPath();
        assertEquals("velocity/deprecated/iframe-page-my-suffix.vm", renderedTemplate);
    }

    @Test
    public void testQueryParams() throws Exception
    {
        StringWriter writer = new StringWriter();
        PageInfo pageInfo = new PageInfo("my-decorator", "-my-suffix", "my-title", new AlwaysDisplayCondition(), Maps.<String, String>newHashMap());
        Map<String, String[]> queryParams = ImmutableMap.<String, String[]>builder()
                .put("a", new String[]{ "111" })
                .put("b", new String[]{ "222" })
                .build();
        iframePageRenderer.renderPage(createContext("a.b", "my-path", "my-namespace"), pageInfo, "", queryParams, "jim", emptyContext(), writer);

        Map<String, Object> context = getActualTemplateRendererContext();
        Map<String, Object> contextParams = (Map<String, Object>) context.get("queryParams");
        assertEquals(contextParams.get("a"), ImmutableList.of("111"));
        assertEquals(contextParams.get("b"), ImmutableList.of("222"));
    }

    private Map<String, Object> getActualTemplateRendererContext() throws IOException
    {
        ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        verify(templateRenderer).render(anyString(), argument.capture(), any(Writer.class));
        return argument.getValue();
    }

    private String getActualTemplateRendererPath() throws IOException
    {
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(templateRenderer).render(argument.capture(), any(Map.class), any(Writer.class));
        return argument.getValue();
    }
}
