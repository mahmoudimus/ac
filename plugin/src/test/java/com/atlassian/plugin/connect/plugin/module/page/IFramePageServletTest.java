package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class IFramePageServletTest
{
    @Mock
    private PageInfo pageInfo;

    @Mock
    private IFrameRenderer iFrameRenderer;

    @Mock
    private IFrameContext iframeContext;

    @Mock
    private UserManager userManager;

    @Mock
    private UrlVariableSubstitutor urlVariableSubstitutor;

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse resp;

    @Test
    public void shouldFoo() throws ServletException, IOException
    {
        final IFramePageServlet servlet = new IFramePageServlet(pageInfo, iFrameRenderer, iframeContext, userManager, urlVariableSubstitutor);
        servlet.doGet(req, resp);
    }
}
