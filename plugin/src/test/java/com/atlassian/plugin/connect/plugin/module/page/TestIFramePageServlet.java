package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.connect.api.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.iframe.page.IFramePageRenderer;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.page.IFramePageServlet;
import com.atlassian.plugin.connect.spi.module.page.PageInfo;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class TestIFramePageServlet
{
    @Mock private UserManager userManager;
    @Mock private UrlVariableSubstitutor urlVariableSubstitutor;
    @Mock private PageInfo pageInfo;
    @Mock private IFrameContext iframeContext;
    @Mock private IFramePageRenderer iFramePageRenderer;
    private Map<String, String> contextParamNameToSymbolicName; // e.g. "my_space_id": "space.id"

    private IFramePageServlet servlet;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        contextParamNameToSymbolicName = newHashMap();
        servlet = new IFramePageServlet(pageInfo, iFramePageRenderer, iframeContext, userManager, urlVariableSubstitutor,
                contextParamNameToSymbolicName);
    }

    @Test
    public void testProductContext() throws Exception
    {
        doGet(ImmutableMap.of("product-context", new String[]{"{\"hello\":\"world\"}"}));

        Map<String, Object> productContext = getActualProductContext();
        assertEquals("world", productContext.get("hello"));
    }

    @Test
    public void testArbitraryParams() throws Exception
    {
        contextParamNameToSymbolicName.put("foo.bar", "foo.bar");
        doGet(ImmutableMap.of("foo.bar", new String[]{"baz"}));

        Map<String, Object> productContext = getActualProductContext();
        assertEquals("baz", productContext.get("foo.bar"));
    }

    private void doGet(Map<String, String[]> requestParams) throws ServletException, IOException
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(requestParams);
        for (Map.Entry<String, String[]> requestParam : requestParams.entrySet())
        {
            when(request.getParameter(requestParam.getKey())).thenReturn(requestParam.getValue()[0]);
        }
        HttpServletResponse response = mock(HttpServletResponse.class);
        servlet.doGet(request, response);
    }

    private Map<String, Object> getActualProductContext() throws IOException
    {
        ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        verify(iFramePageRenderer).renderPage(any(IFrameContext.class), any(PageInfo.class), anyString(), anyMap(), anyString(), argument.capture(), any(Writer.class));
        return argument.getValue();
    }
}
