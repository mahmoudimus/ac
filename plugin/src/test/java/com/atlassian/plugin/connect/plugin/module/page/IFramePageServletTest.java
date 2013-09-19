package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IFramePageServletTest
{
    @Mock
    private PageInfo pageInfo;

    @Mock
    private IFrameRenderer iFrameRenderer;

    @Mock
    private UserManager userManager;

    @Mock
    private UrlVariableSubstitutor urlVariableSubstitutor;

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse resp;

    @Mock
    private PrintWriter out;

    @Mock
    private IFrameParams iFrameParams;

    @Mock
    private ContextMapURLSerializer contextMapURLSerializer;

    @Test
    public void shouldSubstituteVariablesAndCallRender() throws ServletException, IOException
    {
        String iFramePathTemplate = "/foo/bar?arg1=${blah.id}&arg2=${user.address.street}";
        IFrameContext initialIFrameContext = new IFrameContextImpl("myKey", iFramePathTemplate, "namespace1", iFrameParams);

        String[] anotherParamValue = {"88"};
        Map<String, Object> params = ImmutableMap.<String, Object>builder()
                .put("blah.id", new String[] {"10"})
                .put("user.address.street", new String[]{"deadEndSt"})
                .put("notInUrlTemplate", anotherParamValue)
                .build();

        when(req.getPathInfo()).thenReturn("/some/path");
        when(req.getParameterMap()).thenReturn(params);

        when(userManager.getRemoteUsername(req)).thenReturn("barney");
        when(resp.getWriter()).thenReturn(out);

        when(urlVariableSubstitutor.replace(iFramePathTemplate, params)).thenReturn("/foo/bar?arg1=10&arg2=deadEndSt");
        when(urlVariableSubstitutor.getContextVariables(iFramePathTemplate)).thenReturn(ImmutableSet.of(
                "blah.id", "user.address.street"));

        when(contextMapURLSerializer.getAuthenticatedAddonParameters(params, "barney")).thenReturn(params);

        IFramePageServlet servlet = new IFramePageServlet(pageInfo, iFrameRenderer, initialIFrameContext, userManager,
                urlVariableSubstitutor, contextMapURLSerializer
        );
        servlet.doGet(req, resp);

        Map<String, String[]> contextParams = ImmutableMap.of("notInUrlTemplate", anotherParamValue);

        ArgumentCaptor<IFrameContext> argumentCaptor = ArgumentCaptor.forClass(IFrameContext.class);

        verify(iFrameRenderer, times(1)).renderPage(
                argumentCaptor.capture(),
                eq(pageInfo), eq("/some/path"), eq(contextParams), eq("barney"), eq(out));

        IFrameContext capturedIFrameContext = argumentCaptor.getValue();

        assertThat(capturedIFrameContext.getIframePath(), is(equalTo("/foo/bar?arg1=10&arg2=deadEndSt")));
        assertThat(capturedIFrameContext.getIFrameParams(), is(equalTo(iFrameParams)));
        assertThat(capturedIFrameContext.getNamespace(), is(equalTo("namespace1")));
        assertThat(capturedIFrameContext.getPluginKey(), is(equalTo("myKey")));

        verify(resp, times(1)).setContentType("text/html");
    }

    @Test
    public void shouldReturn403IfUnauthorisedContextParamsPassed()
    {
        fail("Not implemented");
    }
}
