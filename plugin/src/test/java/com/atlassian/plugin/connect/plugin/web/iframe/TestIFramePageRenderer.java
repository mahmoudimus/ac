package com.atlassian.plugin.connect.plugin.web.iframe;

import com.atlassian.plugin.connect.plugin.web.page.IFramePageRendererImpl;
import com.atlassian.plugin.connect.plugin.web.HostApplicationInfo;
import com.atlassian.plugin.connect.spi.iframe.page.IFramePageRenderer;
import com.atlassian.plugin.connect.spi.module.page.PageInfo;
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

import static com.atlassian.plugin.connect.plugin.web.iframe.TestIFrameRenderer.createContext;
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
        this.iframePageRenderer = new IFramePageRendererImpl(templateRenderer, iframeRenderer, hostApplicationInfo);
    }

    @Test
    public void testFailingCondition() throws Exception
    {
        StringWriter writer = new StringWriter();
        PageInfo pageInfo = new PageInfo("my-decorator", "-my-suffix", "my-title", new InvertedCondition(new AlwaysDisplayCondition()), Maps.<String, String>newHashMap());
        iframePageRenderer.renderPage(TestIFrameRenderer.createContext("a.b", "my-path", "my-namespace"), pageInfo, "", TestIFrameRenderer.emptyParams(), "jim", TestIFrameRenderer.emptyContext(), writer);

        String renderedTemplate = getActualTemplateRendererPath();
        assertEquals("velocity/deprecated/iframe-page-accessdenied-my-suffix.vm", renderedTemplate);
    }

    @Test
    public void testSucceedingCondition() throws Exception
    {
        StringWriter writer = new StringWriter();
        PageInfo pageInfo = new PageInfo("my-decorator", "-my-suffix", "my-title", new AlwaysDisplayCondition(), Maps.<String, String>newHashMap());
        iframePageRenderer.renderPage(TestIFrameRenderer.createContext("a.b", "my-path", "my-namespace"), pageInfo, "", TestIFrameRenderer.emptyParams(), "jim", TestIFrameRenderer.emptyContext(), writer);

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
        iframePageRenderer.renderPage(TestIFrameRenderer.createContext("a.b", "my-path", "my-namespace"), pageInfo, "", queryParams, "jim", TestIFrameRenderer.emptyContext(), writer);

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
