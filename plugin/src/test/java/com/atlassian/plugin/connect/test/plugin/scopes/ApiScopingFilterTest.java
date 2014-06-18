package com.atlassian.plugin.connect.test.plugin.scopes;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.security.auth.trustedapps.KeyFactory;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.module.permission.ApiScopingFilter;
import com.atlassian.plugin.connect.plugin.product.WebSudoService;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploderUnitTestHelper;
import com.atlassian.plugin.connect.spi.event.ScopedRequestAllowedEvent;
import com.atlassian.plugin.connect.spi.event.ScopedRequestDeniedEvent;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.*;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class ApiScopingFilterTest
{
    private static final String THIS_ADD_ON_KEY = "ac";
    private static final String ADD_ON_KEY = "my-add-on";

    @Mock
    private PermissionManager permissionManager;
    @Mock
    private UserManager userManager;
    @Mock
    ConsumerService consumerService;
    @Mock
    private WebSudoService webSudoService;
    @Mock
    private JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;
    @Mock
    private EventPublisher eventPublisher;

    private ApiScopingFilter apiScopingFilter;
    private UserKey userKey = new UserKey("12345");

    @BeforeClass
    public static void beforeAnyTest()
    {
        XmlDescriptorExploderUnitTestHelper.runBeforeTests();
    }

    @Before
    public void setup()
    {
        when(request.getRequestURI()).thenReturn("/confluence/rest/xyz");
        when(request.getContextPath()).thenReturn("/confluence");

        when(userManager.getRemoteUserKey(any(HttpServletRequest.class))).thenReturn(userKey);
        when(consumerService.getConsumer()).thenReturn(Consumer.key(THIS_ADD_ON_KEY).name("whatever").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).publicKey(new KeyFactory.InvalidPublicKey(new Exception())).build());
        apiScopingFilter = new ApiScopingFilter(permissionManager, userManager, consumerService, webSudoService, jsonConnectAddOnIdentifierService, eventPublisher);
    }

    @Test
    public void testScopeIsCheckedForNonXDMRequests() throws Exception
    {
        when(request.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(ADD_ON_KEY);

        apiScopingFilter.doFilter(request, response, chain);
        verify(permissionManager).isRequestInApiScope(any(HttpServletRequest.class), eq(ADD_ON_KEY), eq(userKey));
    }

    @Test
    public void testScopeIsCheckedForJSONModuleXDMRequests() throws Exception
    {
        when(request.getHeader(ApiScopingFilter.AP_REQUEST_HEADER)).thenReturn(ADD_ON_KEY);
        when(jsonConnectAddOnIdentifierService.isConnectAddOn(ADD_ON_KEY)).thenReturn(true);

        apiScopingFilter.doFilter(request, response, chain);
        verify(permissionManager).isRequestInApiScope(any(HttpServletRequest.class), eq(ADD_ON_KEY), eq(userKey));
    }

    @Test
    public void testScopeIsNotCheckedForXMLModuleXDMRequests() throws Exception
    {
        when(request.getHeader(ApiScopingFilter.AP_REQUEST_HEADER)).thenReturn(ADD_ON_KEY);
        when(jsonConnectAddOnIdentifierService.isConnectAddOn(ADD_ON_KEY)).thenReturn(false);

        apiScopingFilter.doFilter(request, response, chain);
        verify(permissionManager, never()).isRequestInApiScope(any(HttpServletRequest.class), eq(ADD_ON_KEY), eq(userKey));
    }

    @Test
    public void testScopeIsNotCheckedForNonAddOnRequests() throws Exception
    {
        when(request.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(THIS_ADD_ON_KEY);

        apiScopingFilter.doFilter(request, response, chain);
        verify(permissionManager, never()).isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class));
    }

    @Test
    public void testScopeIsNotCheckedForMissingAddOnKey() throws Exception
    {
        apiScopingFilter.doFilter(request, response, chain);
        verify(permissionManager, never()).isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class));
    }

    @Test
    public void testDeniedApiAccessPublishesDeniedEvent() throws Exception
    {
        when(request.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(ADD_ON_KEY);
        when(permissionManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(false);
        apiScopingFilter.doFilter(request, response, chain);
        verify(eventPublisher).publish(argThat(isScopeRequestDeniedEvent()));
    }

    @Test
    public void testDeniedApiAccessDoesntPublishAllowedEvent() throws Exception
    {
        when(request.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(ADD_ON_KEY);
        when(permissionManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(false);
        apiScopingFilter.doFilter(request, response, chain);
        verify(eventPublisher, never()).publish(argThat(isScopeRequestAllowedEvent()));
    }

    @Test
    public void testAllowedApiAccessPublishesEvent() throws Exception
    {
        when(request.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(ADD_ON_KEY);
        when(permissionManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(true);
        apiScopingFilter.doFilter(request, response, chain);
        verify(eventPublisher).publish(argThat(isScopeRequestAllowedEvent()));
    }

    @Test
    public void testAllowedApiAccessDoesntPublishDeniedEvent() throws Exception
    {
        when(request.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(ADD_ON_KEY);
        when(permissionManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(true);
        apiScopingFilter.doFilter(request, response, chain);
        verify(eventPublisher, never()).publish(argThat(isScopeRequestDeniedEvent()));
    }

    private Matcher<Object> isScopeRequestAllowedEvent()
    {
        return new ArgumentMatcher<Object>()
        {
            @Override
            public boolean matches(final Object argument)
            {
                return argument instanceof ScopedRequestAllowedEvent;
            }
        };
    }

    private Matcher<Object> isScopeRequestDeniedEvent()
    {
        return new ArgumentMatcher<Object>()
        {
            @Override
            public boolean matches(final Object argument)
            {
                return argument instanceof ScopedRequestDeniedEvent;
            }
        };
    }
}
