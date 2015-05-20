package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jwt.core.Clock;
import com.atlassian.plugin.connect.api.scopes.AddOnKeyExtractor;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.spi.event.ScopedRequestAllowedEvent;
import com.atlassian.plugin.connect.spi.event.ScopedRequestDeniedEvent;
import com.atlassian.plugin.connect.spi.event.ScopedRequestEvent;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Date;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class ApiScopingFilterTest
{
    private static final String ADD_ON_KEY = "my-add-on";

    @Mock
    private AddOnScopeManager addOnScopeManager;
    @Mock
    private UserManager userManager;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private Clock clock;
    @Mock
    AddOnKeyExtractor addOnKeyExtractor;

    private ApiScopingFilter apiScopingFilter;
    private UserKey userKey = new UserKey("12345");

    @Before
    public void setup()
    {
        when(request.getRequestURI()).thenReturn("/confluence/rest/xyz");
        when(request.getContextPath()).thenReturn("/confluence");
        when(userManager.getRemoteUserKey(any(HttpServletRequest.class))).thenReturn(userKey);
        when(clock.now()).thenReturn(new Date(0));

        apiScopingFilter = new ApiScopingFilter(addOnScopeManager, userManager, eventPublisher, addOnKeyExtractor, clock);
    }

    @Test
    public void testScopeIsCheckedForAddonRequestWithKey() throws Exception
    {
        whenIsAddonRequestWithAddonKey();
        apiScopingFilter.doFilter(request, response, chain);
        verify(addOnScopeManager).isRequestInApiScope(any(HttpServletRequest.class), eq(ADD_ON_KEY), eq(userKey));
    }

    @Test
    public void testScopeIsNotCheckedForNonAddOnRequests() throws Exception
    {
        when(addOnKeyExtractor.isAddOnRequest(request)).thenReturn(false);
        apiScopingFilter.doFilter(request, response, chain);
        verify(addOnScopeManager, never()).isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class));
    }

    @Test
    public void testScopeIsNotCheckedForMissingAddOnKey() throws Exception
    {
        when(addOnKeyExtractor.isAddOnRequest(request)).thenReturn(true);
        when(addOnKeyExtractor.getAddOnKeyFromHttpRequest(request)).thenReturn(null);
        apiScopingFilter.doFilter(request, response, chain);
        verify(addOnScopeManager, never()).isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class));
    }


    @Test
    public void testDeniedApiAccessPublishesDeniedEvent() throws Exception
    {
        whenIsAddonRequestWithAddonKey();
        when(addOnScopeManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(false);
        apiScopingFilter.doFilter(request, response, chain);
        verify(eventPublisher).publish(argThat(isScopeRequestDeniedEvent()));
    }

    @Test
    public void testDeniedApiAccessDoesntPublishAllowedEvent() throws Exception
    {
        when(addOnScopeManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(false);
        apiScopingFilter.doFilter(request, response, chain);
        verify(eventPublisher, never()).publish(argThat(isScopeRequestAllowedEvent()));
    }

    @Test
    public void testAllowedApiAccessPublishesEvent() throws Exception
    {
        whenIsAddonRequestWithAddonKey();
        when(addOnScopeManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(true);
        apiScopingFilter.doFilter(request, response, chain);
        verify(eventPublisher).publish(argThat(isScopeRequestAllowedEvent()));
    }

    @Test
    public void testAllowedApiAccessDoesntPublishDeniedEvent() throws Exception
    {
        whenIsAddonRequestWithAddonKey();
        when(addOnScopeManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(true);
        apiScopingFilter.doFilter(request, response, chain);
        verify(eventPublisher, never()).publish(argThat(isScopeRequestDeniedEvent()));
    }
    
    @Test
    public void testURIsAreTrimmedInDeniedEvents() throws IOException, ServletException
    {
        whenIsAddonRequestWithAddonKey();
        when(addOnScopeManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(false);
        when(request.getRequestURI()).thenReturn("http://localhost/jira/rest/atlassian-connect/1/foo/private-stuff");
        apiScopingFilter.doFilter(request, response, chain);
        verify(eventPublisher).publish(argThat(hasRequestURI("atlassian-connect/1/foo")));
    }

    @Test 
    public void testURIsAreTrimmedInAllowedEvents() throws IOException, ServletException
    {
        whenIsAddonRequestWithAddonKey();
        when(addOnScopeManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(true);
        when(request.getRequestURI()).thenReturn("http://localhost/jira/rest/atlassian-connect/1/foo/private-stuff");
        apiScopingFilter.doFilter(request, response, chain);
        verify(eventPublisher).publish(argThat(hasRequestURI("atlassian-connect/1/foo")));
    }
    
    @Test
    public void testAllowedEventsADuration() throws IOException, ServletException
    {
        whenIsAddonRequestWithAddonKey();
        when(addOnScopeManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(true);
        Date start = new Date(0);
        Date end = new Date(101);
        when(clock.now()).thenReturn(start, end);
        apiScopingFilter.doFilter(request, response, chain);
        verify(eventPublisher).publish(argThat(hasDuration(1)));
    }

    @Test
    public void testAllowedEventsHaveStatusCode() throws IOException, ServletException
    {
        whenIsAddonRequestWithAddonKey();
        when(addOnScopeManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(true);
        FilterChain wrappedChain = new FilterChainWrapper();
        apiScopingFilter.doFilter(request, response, wrappedChain);
        verify(eventPublisher).publish(argThat(hasResponseCode(200)));
    }

    private class FilterChainWrapper implements FilterChain {

        @Override
        public void doFilter(ServletRequest rq, ServletResponse resp) throws IOException, ServletException
        {
            HttpServletResponse.class.cast(resp).setStatus(200);
            chain.doFilter(rq, resp);
        }
    }

    @Test(expected=ServletException.class)
    public void testUnhandledErrorsInFilterChainCreateEvents() throws IOException, ServletException
    {
        whenIsAddonRequestWithAddonKey();
        when(addOnScopeManager.isRequestInApiScope(any(HttpServletRequest.class), anyString(), any(UserKey.class))).thenReturn(true);
        doThrow(new IOException("Something went wrong")).when(chain).doFilter(any(HttpServletRequest.class),
                                                                              any(HttpServletResponse.class));
        try
        {
            apiScopingFilter.doFilter(request, response, chain);
        }
        finally
        {
            verify(eventPublisher).publish(argThat(hasResponseCode(500)));
        }
    }

    private static TypeSafeMatcher<ScopedRequestEvent> hasRequestURI(final String uri)
    {
        return new TypeSafeMatcher<ScopedRequestEvent>()
        {

            @Override
            public void describeTo(Description description)
            {
                description.appendText("got a ScopedRequestEvent with URI: ").appendValue(uri);

            }

            @Override
            protected boolean matchesSafely(ScopedRequestEvent item)
            {
                return item.getHttpRequestUri().equals(uri);
            }

        };
    }

    private static TypeSafeMatcher<ScopedRequestAllowedEvent> hasResponseCode(final int responseCode)
    {
        return new TypeSafeMatcher<ScopedRequestAllowedEvent>()
        {

            @Override
            public void describeTo(Description description)
            {
                description.appendText("got a ScopedRequestEvent with statusCode: ").appendValue(responseCode);

            }

            @Override
            protected boolean matchesSafely(ScopedRequestAllowedEvent item)
            {
                return item.getResponseCode() == responseCode;
            }

        };
    }

    private static TypeSafeMatcher<ScopedRequestAllowedEvent> hasDuration( final long duration)
    {
        return new TypeSafeMatcher<ScopedRequestAllowedEvent>(){

            @Override
            public void describeTo(Description description)
            {
                description.appendText("ScopedRequestEvent duration was: " + duration);
                
            }

            @Override
            protected boolean matchesSafely(ScopedRequestAllowedEvent item)
            {
                return item.getDuration() == duration;
            }};
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

    private void whenIsAddonRequestWithAddonKey()
    {
        when(addOnKeyExtractor.isAddOnRequest(request)).thenReturn(true);
        when(addOnKeyExtractor.getAddOnKeyFromHttpRequest(request)).thenReturn(ADD_ON_KEY);
    }
}
