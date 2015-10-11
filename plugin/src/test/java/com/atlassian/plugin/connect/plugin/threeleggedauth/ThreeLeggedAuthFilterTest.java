package com.atlassian.plugin.connect.plugin.threeleggedauth;

import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.spi.user.ConnectUserService;
import com.atlassian.sal.api.auth.AuthenticationListener;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ThreeLeggedAuthFilterTest
{

    @InjectMocks
    private ThreeLeggedAuthFilter filter;

    @Mock
    private ThreeLeggedAuthService threeLeggedAuthServiceMock;

    @Mock
    private ConnectAddonAccessor addonAccessorMock;

    @Mock
    private UserManager userManagerMock;

    @Mock
    private AuthenticationListener authenticationListenerMock;

    @Mock
    private JwtApplinkFinder jwtApplinkFinderMock;

    @Mock
    private ConnectUserService connectUserServiceMock;

    @Mock
    private I18nResolver i18nResolverMock;

    @Mock
    private HttpServletRequest httpServletRequestMock;

    @Mock
    private HttpServletResponse httpServletResponseMock;

    @Mock
    private FilterChain filterChainMock;

    @Test
    public void shouldSkipIfNotStarted() throws IOException, ServletException
    {
        filter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
        verify(filterChainMock).doFilter(httpServletRequestMock, httpServletResponseMock);
        verifyNoMoreInteractions(httpServletRequestMock);
        verifyNoMoreInteractions(httpServletResponseMock);
    }

    @Test
    public void shouldNotInvokeAddonAccessorForNonJwtRequest() throws IOException, ServletException
    {
        filter.onStart();
        filter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
        verify(filterChainMock).doFilter(httpServletRequestMock, httpServletResponseMock);
        verifyNoMoreInteractions(addonAccessorMock);
    }
}
