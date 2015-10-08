package com.atlassian.plugin.connect.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.jira.security.auth.trustedapps.KeyFactory;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.JwtService;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilderFactory;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilderFactory;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.util.auth.TestJwtJsonBuilderFactory;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;

import static com.atlassian.jwt.JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtClaimMatcher.hasJwtClaim;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtClaimMatcher.hasJwtClaimWithValue;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtContextClaimMatcher.hasJwtContextKey;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtContextClaimMatcher.hasJwtContextKeyWithValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class JwtAuthorizationGeneratorTest
{
    private static final String A_MOCK_JWT = "a.mock.jwt";
    private static final Map<String, String[]> PARAMS = ImmutableMap.of("a_param", new String[]{"a_value"});
    private static final ImmutableMap<String, String[]> ALL_PARAMS = ImmutableMap.of("a_param", new String[] { "a_value" }, "b_param", new String[] { "b value with spaces" }, "c_param", new String[] { "c_value" });
    private static final URI A_URI_BASE = URI.create("http://any.url");
    private static final URI A_URI = URI.create(A_URI_BASE.toString() + "/path?b_param=b+value+with+spaces&c_param=c_value");
    private static final String SECRET = "secret";
    private static final String USER_KEY = "bruceWayne";
    private static final String USER_NAME = "bwayne";
    private static final String USER_DISPLAY_NAME = "Bruce 'Batman' Wayne";

    @Mock
    private JwtService jwtService;
    @Mock
    private ConsumerService consumerService;
    @Mock
    private UserManager userManager;

    private AuthorizationGenerator generator;
    private JwtJsonBuilderFactory jwtBuilderFactory;

    @Test
    public void authorizationHeaderContainsJwt()
    {
        assertThat(generate(), is(Option.some(JWT_AUTH_HEADER_PREFIX + A_MOCK_JWT)));
    }

    @Test
    public void hasIssuerClaim()
    {
        verify(jwtService).issueJwt(argThat(hasJwtClaim("iss")), eq(SECRET));
    }

    @Test
    public void hasIssuedAtClaim()
    {
        verify(jwtService).issueJwt(argThat(hasJwtClaim("iat")), eq(SECRET));
    }

    @Test
    public void hasExpiresAtClaim()
    {
        verify(jwtService).issueJwt(argThat(hasJwtClaim("exp")), eq(SECRET));
    }

    @Test
    public void subjectClaimIsUserKey()
    {
        verify(jwtService).issueJwt(argThat(hasJwtClaimWithValue("sub", USER_KEY)), eq(SECRET));
    }

    @Test
    public void hasQueryHashClaim()
    {
        verify(jwtService).issueJwt(argThat(hasJwtClaim(JwtConstants.Claims.QUERY_HASH)), eq(SECRET));
    }

    @Test
    public void hasQueryHashClaimWithCorrectValue() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        final String expectedQueryHash = generateQueryHash(HttpMethod.POST, A_URI.getPath(), "", ALL_PARAMS);
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void customContextUserObjectPresent()
    {
        verify(jwtService).issueJwt(argThat(hasJwtContextKey("user")), eq(SECRET));
    }

    @Test
    public void customContextUserKeyIsCorrect()
    {
        verify(jwtService).issueJwt(argThat(hasJwtContextKeyWithValue("user.userKey", USER_KEY)), eq(SECRET));
    }

    @Test
    public void customContextUsernameIsCorrect()
    {
        verify(jwtService).issueJwt(argThat(hasJwtContextKeyWithValue("user.username", USER_NAME)), eq(SECRET));
    }

    @Test
    public void customContextDisplayNameIsCorrect()
    {
        verify(jwtService).issueJwt(argThat(hasJwtContextKeyWithValue("user.displayName", USER_DISPLAY_NAME)), eq(SECRET));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHttpMethodResultsInException()
    {
        generator.generate(null, A_URI, PARAMS);
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
        new JwtAuthorizationGenerator(jwtService, jwtBuilderFactory, constantSecretSupplier(SECRET), consumerService, null);
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

        UserProfile userProfile = mock(UserProfile.class);
        when(userManager.getRemoteUser()).thenReturn(userProfile);
        when(userProfile.getFullName()).thenReturn(USER_DISPLAY_NAME);
        when(userProfile.getUsername()).thenReturn(USER_NAME);
        when(userProfile.getUserKey()).thenReturn(new UserKey(USER_KEY));

        jwtBuilderFactory = new TestJwtJsonBuilderFactory(new SubjectJwtClaimWriter(userManager));
        generator = new JwtAuthorizationGenerator(jwtService, jwtBuilderFactory, constantSecretSupplier(SECRET), consumerService, A_URI_BASE);
        generate();
    }

    private Supplier<String> constantSecretSupplier(final String secret)
    {
        return new Supplier<String>()
        {
            @Override
            public String get()
            {
                return secret;
            }
        };
    }

    private ArgumentMatcher<String> hasQueryHash(String expectedQueryHash)
    {
        return hasJwtClaimWithValue(JwtConstants.Claims.QUERY_HASH, expectedQueryHash);
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
        return new JwtAuthorizationGenerator(jwtService, jwtBuilderFactory, constantSecretSupplier(SECRET), consumerService, URI.create(baseUrl)).generate(HttpMethod.GET, URI.create(url), params).get();
    }
}
