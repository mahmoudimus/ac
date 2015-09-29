package it.com.atlassian.plugin.connect.jira.scopes.manager;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.scopes.AddOnScopeManager;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScopeTestDataMatcherFactory
{
    final Map<ScopeName, Plugin> installedAddOns;

    UserKey USER = null;

    public ScopeTestDataMatcherFactory(final Map<ScopeName, Plugin> installedAddOns)
    {
        this.installedAddOns = installedAddOns;
    }

    public Iterable<Matcher<? super AddOnScopeManager>> toScopeTestDataMatchers(Iterable<ScopeTestData> scopeTestData)
    {
        return Iterables.transform(scopeTestData, new Function<ScopeTestData, Matcher<? super AddOnScopeManager>>()
        {
            @Override
            public Matcher<? super AddOnScopeManager> apply(final ScopeTestData data)
            {
                return performsCorrectActionForScope(data);
            }
        });
    }

    public Matcher<? super AddOnScopeManager> performsCorrectActionForScope(final ScopeTestData data)
    {
        return new TypeSafeMatcher<AddOnScopeManager>()
        {
            @Override
            protected boolean matchesSafely(final AddOnScopeManager scopeManager)
            {
                try
                {
                    return scopeManager.isRequestInApiScope(setupRequest(data), installedAddOns.get(data.scope).getKey(), USER) == data.expectedOutcome;
                }
                catch (IOException e)
                {
                    return false;
                }
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendValue(data.expectedOutcome).appendText("for").appendValue(data);
            }

            private HttpServletRequest setupRequest(final ScopeTestData data) throws IOException
            {
                HttpServletRequest request = mock(HttpServletRequest.class);
                when(request.getContextPath()).thenReturn(data.contextPath);
                when(request.getRequestURI()).thenReturn(data.path);
                when(request.getMethod()).thenReturn(data.method.name());
                when(request.getInputStream()).thenAnswer(new Answer<ServletInputStream>()
                {
                    @Override
                    public ServletInputStream answer(InvocationOnMock invocationOnMock) throws Throwable
                    {
                        return new ServletStringInputStream(data.requestBody);
                    }
                });
                return request;
            }

        };
    }
    private static class ServletStringInputStream extends ServletInputStream
    {
        private final InputStream delegate;

        public ServletStringInputStream(String content) throws IOException
        {
            this.delegate = IOUtils.toInputStream(content, "UTF-8");
        }

        @Override
        public int read() throws IOException
        {
            return delegate.read();
        }
    }
}
