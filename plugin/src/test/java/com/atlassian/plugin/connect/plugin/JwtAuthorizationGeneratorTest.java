package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.fugue.Option;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.jwt.core.JwtUtil;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JwtAuthorizationGeneratorTest
{
    private static final String USER_KEY = "some_user";
    private static final String USER_KEY_PARAM_NAME = "user_key";
    private static final ImmutableMap<String,List<String>> USER_KEY_PARAM_MAP = ImmutableMap.of(USER_KEY_PARAM_NAME, asList(USER_KEY));
    private static final String A_MOCK_JWT = "a.mock.jwt";
    private static final Map<String,List<String>> NO_PARAMS = Collections.emptyMap();
    private static final String SUBJECT_CLAIM_NAME = "sub";

    @Mock private JwtService jwtService;
    @Mock private ApplicationLink applicationLink;

    private AuthorizationGenerator generator;

    @Test
    public void authorizationHeaderContainsJwt()
    {
        assertThat(generate(URI.create("http://any.url/path"), NO_PARAMS), is(Option.some(JwtUtil.JWT_AUTH_HEADER_PREFIX + A_MOCK_JWT)));
    }

    @Test
    public void userKeyInUriResultsInSubjectClaim()
    {
        generate(URI.create("http://any.url/path?" + USER_KEY_PARAM_NAME + "=" + USER_KEY), NO_PARAMS);
        verify(jwtService).issueJwt(argThat(hasAnySubject()), eq(applicationLink));
    }

    @Test
    public void userKeyInUriGoesIntoSubjectClaim()
    {
        generate(URI.create("http://any.url/path?" + USER_KEY_PARAM_NAME + "=" + USER_KEY), NO_PARAMS);
        verify(jwtService).issueJwt(argThat(hasSubject(USER_KEY)), eq(applicationLink));
    }

    @Test
    public void userKeyInParamsResultsInSubjectClaim()
    {
        generate(URI.create("http://any.url/path"), USER_KEY_PARAM_MAP);
        verify(jwtService).issueJwt(argThat(hasAnySubject()), eq(applicationLink));
    }

    @Test
    public void userKeyInParamsGoesIntoSubjectClaim()
    {
        generate(URI.create("http://any.url/path"), USER_KEY_PARAM_MAP);
        verify(jwtService).issueJwt(argThat(hasSubject(USER_KEY)), eq(applicationLink));
    }

    @Test
    public void noUserKeyResultsInNoSubject()
    {
        generate(URI.create("http://any.url/path"), NO_PARAMS);
        verify(jwtService).issueJwt(argThat(hasNoSubject()), eq(applicationLink));
    }

    @Test(expected = IllegalArgumentException.class)
    public void userKeyInUriAndParamsResultsInException()
    {
        generate(URI.create("http://any.url/path?" + USER_KEY_PARAM_NAME + "=" + USER_KEY), USER_KEY_PARAM_MAP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHttpMethodResultsInException()
    {
        generator.generate((HttpMethod) null, URI.create("http://any.path"), NO_PARAMS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullUriResultsInException()
    {
        generator.generate(HttpMethod.GET, null, NO_PARAMS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullParamsMapResultsInException()
    {
        generator.generate(HttpMethod.GET, URI.create("http://any.path"), null);
    }

    @Before
    public void beforeEachTest()
    {
        when(jwtService.issueJwt(any(String.class), eq(applicationLink))).thenReturn(A_MOCK_JWT);
        generator = new JwtAuthorizationGenerator(jwtService, applicationLink);
    }

    private Option<String> generate(URI uri, Map<String, List<String>> queryParams)
    {
        return generator.generate(HttpMethod.POST, uri, queryParams);
    }

    /**
     * @return a Mockito matcher that parses a JSON string for a non-null "sub" field
     */
    public static ArgumentMatcher<String> hasAnySubject()
    {
        return new ArgumentMatcher<String>()
        {
            @Override
            public boolean matches(Object actual)
            {
                return actual instanceof String
                        && !StringUtils.isEmpty((String) actual)
                        && new JsonParser().parse((String)actual).getAsJsonObject().has(SUBJECT_CLAIM_NAME);
            }
        };
    }

    /**
     * @return a Mockito matcher that parses a JSON string for a specific value in "sub" field
     */
    public static ArgumentMatcher<String> hasSubject(final String subject)
    {
        return new ArgumentMatcher<String>()
        {
            @Override
            public boolean matches(Object actual)
            {
                return actual instanceof String
                        && !StringUtils.isEmpty((String) actual)
                        && new JsonParser().parse((String)actual).getAsJsonObject().get(SUBJECT_CLAIM_NAME).getAsString().equals(subject);
            }
        };
    }

    /**
     * @return a Mockito matcher that parses a JSON string for a should-be-missing "sub" field
     */
    public static ArgumentMatcher<String> hasNoSubject()
    {
        return new ArgumentMatcher<String>()
        {
            @Override
            public boolean matches(Object argument)
            {
                return !hasAnySubject().matches(argument);
            }
        };
    }
}
