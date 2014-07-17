package com.atlassian.plugin.connect.plugin.module.util.redirect;

import java.net.MalformedURLException;

import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.module.util.redirect.RedirectOnNotFoundFilter.createRedirectUrl;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RedirectOnNotFoundFilterTest
{
    private static final String REQUEST_URL = "http://localhost:8080/foo/godzilla";
    private static final String REDIRECT_URL = "http://localhost:8080/blah/godzilla";
    private static final String SOME_QUERY_PARAMS = "foo=bar&bar=foo";
    private static final String SOME_ESCAPED_QUERY_PARAMS = "foo=bar%20blah&bar=foo";

    @Test
    public void returnsRequestUrlIfFromPatternDoesNotMatch() throws MalformedURLException
    {
        assertThat(createRedirectUrl(new StringBuffer(REQUEST_URL), null, "notfoo", "blah"), is(REQUEST_URL));
    }

    @Test
    public void substitutesPatternsIfFromPatternDoesMatch() throws MalformedURLException
    {
        assertThat(createRedirectUrl(new StringBuffer(REQUEST_URL), null, "foo", "blah"), is(REDIRECT_URL));
    }

    @Test
    public void emptyQueryStringIgnored() throws MalformedURLException
    {
        assertThat(createRedirectUrl(new StringBuffer(REQUEST_URL), "", "foo", "blah"), is(REDIRECT_URL));
    }

    @Test
    public void queryStringIncluded() throws MalformedURLException
    {
        assertThat(createRedirectUrl(new StringBuffer(REQUEST_URL), SOME_QUERY_PARAMS, "foo", "blah"),
                is(REDIRECT_URL + "?" + SOME_QUERY_PARAMS));
    }

    @Test
    public void doesNotSubstituteInQueryString() throws MalformedURLException
    {
        assertThat(createRedirectUrl(new StringBuffer(REQUEST_URL), SOME_QUERY_PARAMS, "bar", "blah"),
                is(REQUEST_URL + "?" + SOME_QUERY_PARAMS));
    }

    @Test
    public void escapedQueryStringIncluded() throws MalformedURLException
    {
        assertThat(createRedirectUrl(new StringBuffer(REQUEST_URL), SOME_ESCAPED_QUERY_PARAMS, "foo", "blah"),
                is(REDIRECT_URL + "?" + SOME_ESCAPED_QUERY_PARAMS));
    }

    @Test
    public void emptyFromPatternIgnored() throws MalformedURLException
    {
        // Note: the init method would barf in this case so this is rather paranoid
        assertThat(createRedirectUrl(new StringBuffer(REQUEST_URL), null, "", "blah"), is(REQUEST_URL));
    }

    @Test
    public void extraPathSeparatorRemovedIfToPatternEmpty() throws MalformedURLException
    {
        assertThat(createRedirectUrl(new StringBuffer(REQUEST_URL), null, "foo", ""), is("http://localhost:8080/godzilla"));
    }

    @Test
    public void extraPathSeparatorRemovedIfToPatternEmptyX() throws MalformedURLException
    {
        assertThat(createRedirectUrl(new StringBuffer(REQUEST_URL), null, "godzilla", ""), is("http://localhost:8080/foo"));
    }
}