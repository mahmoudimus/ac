package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.connect.plugin.module.context.MalformedRequestException;
import com.atlassian.plugin.connect.plugin.module.context.ResourceNotFoundException;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.atlassian.plugin.connect.plugin.module.webfragment.InvalidContextParameterException;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlTemplateInstance;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlTemplateInstanceFactory;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;
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

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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
    private UrlTemplateInstanceFactory urlTemplateInstanceFactory;

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse resp;

    @Mock
    private PrintWriter out;

    @Mock
    private IFrameParams iFrameParams;

    @Mock
    private UrlTemplateInstance urlTemplateInstance;

    @Test
    public void shouldSubstituteVariablesAndCallRender() throws ServletException, IOException, MalformedRequestException, UnauthorisedException, ResourceNotFoundException
    {
        String iFramePathTemplate = "/foo/bar?arg1=${blah.id}&arg2=${user.address.street}";
        IFrameContext initialIFrameContext = new IFrameContextImpl("myKey", iFramePathTemplate, "namespace1", iFrameParams);

        String[] anotherParamValue = {"88"};
        Map<String, Object> params = ImmutableMap.<String, Object>builder()
                .put("blah.id", new String[]{"10"})
                .put("user.address.street", new String[]{"deadEndSt"})
                .put("notInUrlTemplate", anotherParamValue)
                .build();

        when(req.getPathInfo()).thenReturn("/some/path");
        when(req.getParameterMap()).thenReturn(params);

        when(userManager.getRemoteUsername(req)).thenReturn("barney");
        when(resp.getWriter()).thenReturn(out);

        when(urlTemplateInstanceFactory.create(iFramePathTemplate, params, "barney")).thenReturn(urlTemplateInstance);
        Map<String, String[]> nonTemplateContextParams = ImmutableMap.of("notInUrlTemplate", anotherParamValue);
        when(urlTemplateInstance.getNonTemplateContextParameters()).thenReturn(nonTemplateContextParams);
        when(urlTemplateInstance.getUrlString()).thenReturn("/foo/bar?arg1=10&arg2=deadEndSt");

        IFramePageServlet servlet = new IFramePageServlet(pageInfo, iFrameRenderer, initialIFrameContext, userManager,
                urlTemplateInstanceFactory);
        servlet.doGet(req, resp);


        ArgumentCaptor<IFrameContext> argumentCaptor = ArgumentCaptor.forClass(IFrameContext.class);

        verify(iFrameRenderer, times(1)).renderPage(argumentCaptor.capture(), eq(pageInfo), eq("/some/path"),
                eq(nonTemplateContextParams), eq("barney"), eq(out));

        IFrameContext capturedIFrameContext = argumentCaptor.getValue();

        assertThat(capturedIFrameContext.getIframePath(), is(equalTo("/foo/bar?arg1=10&arg2=deadEndSt")));
        assertThat(capturedIFrameContext.getIFrameParams(), is(equalTo(iFrameParams)));
        assertThat(capturedIFrameContext.getNamespace(), is(equalTo("namespace1")));
        assertThat(capturedIFrameContext.getPluginKey(), is(equalTo("myKey")));

        verify(resp, times(1)).setContentType("text/html");
    }

    @Test
    public void shouldReturn400OnInvalidContextParameterException() throws MalformedRequestException, ServletException,
            IOException, UnauthorisedException, ResourceNotFoundException
    {
        IFrameContext initialIFrameContext = new IFrameContextImpl("myKey", "path", "namespace1", iFrameParams);

        when(urlTemplateInstanceFactory.create(anyString(), anyMap(), anyString()))
                .thenThrow(new InvalidContextParameterException("doh"));

        IFramePageServlet servlet = new IFramePageServlet(pageInfo, iFrameRenderer, initialIFrameContext, userManager,
                urlTemplateInstanceFactory);
        servlet.doGet(req, resp);

        verify(resp, times(1)).sendError(SC_BAD_REQUEST, "doh");
    }

    @Test
    public void shouldReturn401IfUnauthorisedContextParamsPassed() throws MalformedRequestException, IOException, ServletException, UnauthorisedException, ResourceNotFoundException
    {
        IFrameContext initialIFrameContext = new IFrameContextImpl("myKey", "path", "namespace1", iFrameParams);

        when(urlTemplateInstanceFactory.create(anyString(), anyMap(), anyString()))
                .thenThrow(new UnauthorisedException("doh"));

        IFramePageServlet servlet = new IFramePageServlet(pageInfo, iFrameRenderer, initialIFrameContext, userManager,
                urlTemplateInstanceFactory);
        servlet.doGet(req, resp);

        verify(resp, times(1)).sendError(SC_UNAUTHORIZED, "doh");
        // Note: Filters will populate the WWW-Authenticate for us
    }

    @Test
    public void shouldReturn404IfResourceNotFound() throws MalformedRequestException, IOException, ServletException, UnauthorisedException, ResourceNotFoundException
    {
        IFrameContext initialIFrameContext = new IFrameContextImpl("myKey", "path", "namespace1", iFrameParams);

        when(urlTemplateInstanceFactory.create(anyString(), anyMap(), anyString()))
                .thenThrow(new ResourceNotFoundException("doh"));

        IFramePageServlet servlet = new IFramePageServlet(pageInfo, iFrameRenderer, initialIFrameContext, userManager,
                urlTemplateInstanceFactory);
        servlet.doGet(req, resp);

        verify(resp, times(1)).sendError(SC_NOT_FOUND, "doh");
    }

    @Test
    public void shouldReturn400IfMalformedParams() throws MalformedRequestException, IOException, ServletException, UnauthorisedException, ResourceNotFoundException
    {
        IFrameContext initialIFrameContext = new IFrameContextImpl("myKey", "path", "namespace1", iFrameParams);

        when(urlTemplateInstanceFactory.create(anyString(), anyMap(), anyString()))
                .thenThrow(new MalformedRequestException("doh"));

        IFramePageServlet servlet = new IFramePageServlet(pageInfo, iFrameRenderer, initialIFrameContext, userManager,
                urlTemplateInstanceFactory);
        servlet.doGet(req, resp);

        verify(resp, times(1)).sendError(SC_BAD_REQUEST, "doh");
    }
}
