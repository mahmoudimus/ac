package com.atlassian.plugin.connect.plugin.threeleggedauth;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.sal.api.auth.AuthenticationListener;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ThreeLeggedAuthFilterTest
{

    private final ThreeLeggedAuthFilter filter;
    private final ThreeLeggedAuthService threeLeggedAuthServiceMock;
    private final ConnectAddonManager connectAddonManagerMock;
    private final UserManager userManagerMock;
    private final AuthenticationListener authenticationListenerMock;
    private final JwtApplinkFinder jwtApplinkFinderMock;
    private final CrowdService crowdServiceMock;
    private final I18nResolver i18nResolverMock;
    private final HttpServletRequest httpServletRequestMock;
    private final HttpServletResponse httpServletResponseMock;
    private final FilterChain filterChainMock;

    public ThreeLeggedAuthFilterTest()
    {
        threeLeggedAuthServiceMock = mock(ThreeLeggedAuthService.class);
        connectAddonManagerMock = mock(ConnectAddonManager.class);
        userManagerMock = mock(UserManager.class);
        authenticationListenerMock = mock(AuthenticationListener.class);
        jwtApplinkFinderMock = mock(JwtApplinkFinder.class);
        crowdServiceMock = mock(CrowdService.class);
        i18nResolverMock = mock(I18nResolver.class);
        filter = new ThreeLeggedAuthFilter(threeLeggedAuthServiceMock,
                connectAddonManagerMock, userManagerMock, authenticationListenerMock, jwtApplinkFinderMock, crowdServiceMock, i18nResolverMock);

        httpServletRequestMock = mock(HttpServletRequest.class);
        httpServletResponseMock = mock(HttpServletResponse.class);
        filterChainMock = mock(FilterChain.class);
    }

    @Test
    public void shouldSkipIfNotStarted() throws IOException, ServletException
    {
        filter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
        verify(filterChainMock).doFilter(httpServletRequestMock, httpServletResponseMock);
        verifyNoMoreInteractions(httpServletRequestMock);
        verifyNoMoreInteractions(httpServletResponseMock);
    }

    @Test
    public void shouldNotAccessAddonRegistryForNonJwtRequest() throws IOException, ServletException
    {
        filter.onStart();
        filter.doFilter(httpServletRequestMock, httpServletResponseMock, filterChainMock);
        verify(filterChainMock).doFilter(httpServletRequestMock, httpServletResponseMock);
        verifyNoMoreInteractions(connectAddonManagerMock);
    }
}
