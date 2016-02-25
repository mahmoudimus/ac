package com.atlassian.plugin.connect.api.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServletUtilsTest {
    @Mock
    private HttpServletRequest request;

    @Test
    public void testExtractPathInfoWithoutContext() throws Exception {
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/rest/api/2/user");

        assertThat(ServletUtils.extractPathInfo(request), is("/rest/api/2/user"));
    }

    @Test
    public void testExtractPathInfoWithContext() throws Exception {
        when(request.getContextPath()).thenReturn("/jira");
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user");

        assertThat(ServletUtils.extractPathInfo(request), is("/rest/api/2/user"));
    }

    @Test
    public void testExtractPathFromMaliciousUrl() throws Exception {
        when(request.getContextPath()).thenReturn("/jira");
        when(request.getRequestURI()).thenReturn("/jira/untrusted;../../rest/api/2/user");

        assertThat(ServletUtils.extractPathInfo(request), is("/untrusted"));
    }

    @Test
    public void testNormalizedPathShouldBeIdenticalWithoutContext() throws Exception {
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/my/url");

        assertThat(ServletUtils.normalisedAndOriginalRequestUrisDiffer(request), is(false));
    }

    @Test
    public void testNormalizedPathShouldBeIdenticalWithSimpleUrl() throws Exception {
        when(request.getContextPath()).thenReturn("/jira");
        when(request.getRequestURI()).thenReturn("/jira/my/url");

        assertThat(ServletUtils.normalisedAndOriginalRequestUrisDiffer(request), is(false));
    }

    @Test
    public void testNormalizedPathShouldBeSameWithRelativePathAtFront() throws Exception {
        when(request.getContextPath()).thenReturn("/jira");
        when(request.getRequestURI()).thenReturn("/jira/../../rest/api/2/user");

        assertThat(ServletUtils.normalisedAndOriginalRequestUrisDiffer(request), is(false));
    }

    @Test
    public void testNormalizedPathShouldBeDifferentWithRelativePaths() throws Exception {
        when(request.getContextPath()).thenReturn("/jira");
        when(request.getRequestURI()).thenReturn("/jira/hello/world/../../rest/api/2/user");

        assertThat(ServletUtils.normalisedAndOriginalRequestUrisDiffer(request), is(true));
    }

    @Test
    public void testNormalizedPathShouldBeDifferentWithSemiColon() throws Exception {
        when(request.getContextPath()).thenReturn("/jira");
        when(request.getRequestURI()).thenReturn("/jira/foo;../../rest/api/2/user");

        assertThat(ServletUtils.normalisedAndOriginalRequestUrisDiffer(request), is(true));
    }
}
