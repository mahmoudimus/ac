package com.atlassian.plugin.connect.test.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.jira.security.auth.trustedapps.KeyFactory;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.plugin.JwtAuthorizationGenerator;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;

import static com.atlassian.jwt.JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class JwtAuthorizationGeneratorTest
{
    private static final String A_MOCK_JWT = "a.mock.jwt";
    private static final Map<String, String[]> PARAMS = ImmutableMap.of("a_param", new String[]{"a_value"});
    private static final ImmutableMap<String, String[]> ALL_PARAMS = ImmutableMap.of("a_param", new String[]{"a_value"}, "b_param", new String[]{"b value with spaces"}, "c_param", new String[]{"c_value"});
    private static final URI A_URI_BASE = URI.create("http://any.url");
    private static final URI A_URI = URI.create(A_URI_BASE.toString() + "/path?b_param=b+value+with+spaces&c_param=c_value");
    private static final String SECRET = "secret";

    @Mock
    private JwtService jwtService;
    @Mock
    private ConsumerService consumerService;

    private AuthorizationGenerator generator;

    @Test
    public void authorizationHeaderContainsJwt()
    {
        assertThat(generate(), is(Option.some(JWT_AUTH_HEADER_PREFIX + A_MOCK_JWT)));
    }

    @Test
    public void hasIssuerClaim()
    {
        verify(jwtService).issueJwt(argThat(hasClaim("iss")), eq(SECRET));
    }

    @Test
    public void hasIssuedAtClaim()
    {
        verify(jwtService).issueJwt(argThat(hasClaim("iat")), eq(SECRET));
    }

    @Test
    public void hasExpiresAtClaim()
    {
        verify(jwtService).issueJwt(argThat(hasClaim("exp")), eq(SECRET));
    }

    @Test
    public void hasNoSubjectClaim()
    {
        verify(jwtService).issueJwt(argThat(hasNoSuchClaim("sub")), eq(SECRET));
    }

    @Test
    public void hasQueryHashClaim()
    {
        verify(jwtService).issueJwt(argThat(hasClaim(JwtConstants.Claims.QUERY_HASH)), eq(SECRET));
    }

    @Test
    public void hasQueryHashClaimWithCorrectValue() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        final String expectedQueryHash = generateQueryHash(HttpMethod.POST, A_URI.getPath(), "", ALL_PARAMS);
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHttpMethodResultsInException()
    {
        generator.generate((HttpMethod) null, A_URI, PARAMS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullUriResultsInException()
    {
        generator.generate(HttpMethod.GET, null, PARAMS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullParamsMapResultsInException()
    {
        generator.generate(HttpMethod.GET, A_URI, null);
    }

    @Test(expected = NullPointerException.class)
    public void nullBaseUrlResultsInException()
    {
        new JwtAuthorizationGenerator(jwtService, SECRET, consumerService, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void absoluteUrlThatDoesNotStartWithTheBaseUrlResultsInException()
    {
        generateGet("https://example.com/foo", "https://other.domain", PARAMS);
    }

    @Test
    public void signedRelativeUrlWithNoBaseIsEquivalentToAbsoluteUrlWithAnyBase() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/path", "", ALL_PARAMS);
        generateGet("/path", "https://example.com", ALL_PARAMS);
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void signedUrlDoesNotIncludeAddOnBaseUrlWhenBaseUrlContainsPath() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/and/path", "", Collections.<String, String[]>emptyMap());
        generateGet("https://example.com/base/and/path", "https://example.com/base", Collections.<String, String[]>emptyMap());
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void signedUrlDoesNotIncludeAddOnBaseUrlWhenBaseUrlContainsPathAndThereAreParams() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/and/path", "", PARAMS);
        generateGet("https://example.com/base/and/path", "https://example.com/base", PARAMS);
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void signedUrlDoesNotIncludeAddOnBaseUrlWhenBaseUrlContainsPathAndThereAreParamsInTheTargetPath() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/and/path", "", PARAMS);
        generateGet("https://example.com/base/and/path?a_param=a_value", "https://example.com/base", Collections.<String, String[]>emptyMap());
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void signedUrlDoesNotIncludeAddOnBaseUrlWhenBaseUrlEndsInSlash() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/path", "", ALL_PARAMS);
        generateGet("https://example.com/path", "https://example.com/", ALL_PARAMS);
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void signedUrlPathIsSlashWhenThereIsImplicitlyNoPathRelativeToTheBaseUrl() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/", "", ALL_PARAMS);
        generateGet("https://example.com/base", "https://example.com/base", ALL_PARAMS);
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Before
    public void beforeEachTest()
    {
        when(jwtService.issueJwt(any(String.class), eq(SECRET))).thenReturn(A_MOCK_JWT);
        when(consumerService.getConsumer()).thenReturn(Consumer.key("whatever").name("whatever").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).publicKey(new KeyFactory.InvalidPublicKey(new Exception())).build());
        generator = new JwtAuthorizationGenerator(jwtService, SECRET, consumerService, A_URI_BASE);
        generate();
    }

    private ArgumentMatcher<String> hasQueryHash(String expectedQueryHash)
    {
        return hasClaim(JwtConstants.Claims.QUERY_HASH, expectedQueryHash, true);
    }

    private String generateQueryHash(HttpMethod httpMethod, String targetUrl, String baseUrl, Map<String, String[]> params) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        CanonicalHttpUriRequest canonicalRequest = new CanonicalHttpUriRequest(httpMethod.toString(), targetUrl, baseUrl, params);
        return HttpRequestCanonicalizer.computeCanonicalRequestHash(canonicalRequest);
    }

    private Option<String> generate()
    {
        return generator.generate(HttpMethod.POST, A_URI, PARAMS);
    }

    private String generateGet(String url, String baseUrl, Map<String, String[]> params)
    {
        return new JwtAuthorizationGenerator(jwtService, SECRET, consumerService, URI.create(baseUrl)).generate(HttpMethod.GET, URI.create(url), params).get();
    }

    private static ArgumentMatcher<String> hasClaim(final String claimName)
    {
        return hasClaim(claimName, null, false);
    }

    private static ArgumentMatcher<String> hasClaim(final String claimName, final String expectedValue, final boolean checkValue)
    {
        return new ArgumentMatcher<String>()
        {
            @Override
            public boolean matches(Object actual)
            {
                JsonObject json = new JsonParser().parse((String) actual).getAsJsonObject();
                boolean matches = actual instanceof String
                        && !StringUtils.isEmpty((String) actual)
                        && json.has(claimName);

                if (matches && checkValue)
                {
                    String actualClaimValue = json.get(claimName).getAsString();
                    matches = null == actualClaimValue ? null == expectedValue : actualClaimValue.equals(expectedValue);
                }

                return matches;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("JSON encoded string with claim ").appendValue(claimName);

                if (checkValue)
                {
                    description.appendText(" having value ").appendValue(expectedValue);
                }
            }
        };
    }

    private static ArgumentMatcher<String> hasNoSuchClaim(final String claimName)
    {
        return new ArgumentMatcher<String>()
        {
            @Override
            public boolean matches(Object argument)
            {
                return !hasClaim(claimName).matches(argument);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("not ");
                hasClaim(claimName).describeTo(description);
            }
        };
    }
}
