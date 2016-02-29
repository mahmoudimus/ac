package it.com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.AddonScopeManager;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScopeTestDataMatcherFactory {
    final Map<ScopeName, Plugin> installedAddons;

    public ScopeTestDataMatcherFactory(final Map<ScopeName, Plugin> installedAddons) {
        this.installedAddons = installedAddons;
    }

    public Iterable<Matcher<? super AddonScopeManager>> toScopeTestDataMatchers(Iterable<ScopeTestData> scopeTestData) {
        return Iterables.transform(scopeTestData, this::performsCorrectActionForScope);
    }

    public Matcher<? super AddonScopeManager> performsCorrectActionForScope(final ScopeTestData data) {
        return new TypeSafeMatcher<AddonScopeManager>() {
            @Override
            protected boolean matchesSafely(final AddonScopeManager scopeManager) {
                try {
                    return scopeManager.isRequestInApiScope(setupRequest(data), installedAddons.get(data.scope).getKey()) == data.expectedOutcome;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendValue(data.expectedOutcome).appendText("for").appendValue(data);
            }

            private HttpServletRequest setupRequest(final ScopeTestData data) throws IOException {
                HttpServletRequest request = mock(HttpServletRequest.class);
                when(request.getContextPath()).thenReturn(data.contextPath);
                when(request.getRequestURI()).thenReturn(data.path);
                when(request.getMethod()).thenReturn(data.method.name());
                when(request.getInputStream()).thenAnswer(invocationOnMock -> new ServletStringInputStream(data.requestBody));
                return request;
            }

        };
    }

    private static class ServletStringInputStream extends ServletInputStream {
        private final InputStream delegate;

        public ServletStringInputStream(String content) throws IOException {
            this.delegate = IOUtils.toInputStream(content, "UTF-8");
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }
    }
}
