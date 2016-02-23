package com.atlassian.plugin.connect.plugin.auth.jwt;

import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.JwtService;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilderFactory;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.api.auth.AuthorizationGenerator;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Ignore;
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
import java.util.Optional;

import static com.atlassian.jwt.JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtClaimStringMatcher.hasJwtClaim;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtClaimStringMatcher.hasJwtClaimWithValue;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtContextClaimMatcher.hasJwtContextKey;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtContextClaimMatcher.hasJwtContextKeyWithValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class JwtAuthorizationGeneratorTest {
    private static final String A_MOCK_JWT = "a.mock.jwt";
    private static final Map<String, String[]> PARAMS = ImmutableMap.of("a_param", new String[]{"a_value"});
    private static final ImmutableMap<String, String[]> ALL_PARAMS = ImmutableMap.of("a_param", new String[]{"a_value"}, "b_param", new String[]{"b value with spaces"}, "c_param", new String[]{"c_value"});
    private static final String CONTEXT_PATH = "/contextPath";
    private static final URI A_URI_BASE = URI.create("http://any.url" + CONTEXT_PATH);
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
    public void authorizationHeaderContainsJwt() {
        assertThat(generate(), is(Optional.of(JWT_AUTH_HEADER_PREFIX + A_MOCK_JWT)));
    }

    @Test
    public void hasIssuerClaim() {
        verify(jwtService).issueJwt(argThat(hasJwtClaim("iss")), eq(SECRET));
    }

    @Test
    public void hasIssuedAtClaim() {
        verify(jwtService).issueJwt(argThat(hasJwtClaim("iat")), eq(SECRET));
    }

    @Test
    public void hasExpiresAtClaim() {
        verify(jwtService).issueJwt(argThat(hasJwtClaim("exp")), eq(SECRET));
    }

    @Test
    public void subjectClaimIsUserKey() {
        verify(jwtService).issueJwt(argThat(hasJwtClaimWithValue("sub", USER_KEY)), eq(SECRET));
    }

    @Test
    public void hasQueryHashClaim() {
        verify(jwtService).issueJwt(argThat(hasJwtClaim(JwtConstants.Claims.QUERY_HASH)), eq(SECRET));
    }

    @Test
    public void hasQueryHashClaimWithCorrectValue() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        final String expectedQueryHash = generateQueryHash(HttpMethod.POST, A_URI.getPath(), CONTEXT_PATH, ALL_PARAMS);
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void customContextUserObjectPresent() {
        verify(jwtService).issueJwt(argThat(hasJwtContextKey("user")), eq(SECRET));
    }

    @Test
    public void customContextUserKeyIsCorrect() {
        verify(jwtService).issueJwt(argThat(hasJwtContextKeyWithValue("user.userKey", USER_KEY)), eq(SECRET));
    }

    @Test
    public void customContextUsernameIsCorrect() {
        verify(jwtService).issueJwt(argThat(hasJwtContextKeyWithValue("user.username", USER_NAME)), eq(SECRET));
    }

    @Test
    public void customContextDisplayNameIsCorrect() {
        verify(jwtService).issueJwt(argThat(hasJwtContextKeyWithValue("user.displayName", USER_DISPLAY_NAME)), eq(SECRET));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHttpMethodResultsInException() {
        generator.generate(null, A_URI, PARAMS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullUriResultsInException() {
        generator.generate(HttpMethod.GET, null, PARAMS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullParamsMapResultsInException() {
        generator.generate(HttpMethod.GET, A_URI, null);
    }

    @Test(expected = NullPointerException.class)
    public void nullBaseUrlResultsInException() {
        new JwtAuthorizationGenerator(jwtService, jwtBuilderFactory, constantSecretSupplier(SECRET), consumerService, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void absoluteUrlThatDoesNotStartWithTheBaseUrlResultsInException() {
        generateGet("https://example.com/foo", "https://other.domain", PARAMS);
    }

    @Test
    public void signedRelativeUrlWithNoBaseIsEquivalentToAbsoluteUrlWithAnyBase() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/path", CONTEXT_PATH, ALL_PARAMS);
        generateGet("/path", "https://example.com", ALL_PARAMS);
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void signedUrlDoesNotIncludeAddonBaseUrlWhenBaseUrlContainsPath() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/and/path", CONTEXT_PATH, Collections.<String, String[]>emptyMap());
        generateGet("https://example.com/base/and/path", "https://example.com/base", Collections.<String, String[]>emptyMap());
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void signedUrlDoesNotIncludeAddonBaseUrlWhenBaseUrlContainsPathAndThereAreParams() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/and/path", CONTEXT_PATH, PARAMS);
        generateGet("https://example.com/base/and/path", "https://example.com/base", PARAMS);
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void signedUrlDoesNotIncludeAddonBaseUrlWhenBaseUrlContainsPathAndThereAreParamsInTheTargetPath() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/and/path", CONTEXT_PATH, PARAMS);
        generateGet("https://example.com/base/and/path?a_param=a_value", "https://example.com/base", Collections.<String, String[]>emptyMap());
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void signedUrlDoesNotIncludeAddonBaseUrlWhenBaseUrlEndsInSlash() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/path", CONTEXT_PATH, ALL_PARAMS);
        generateGet("https://example.com/path", "https://example.com/", ALL_PARAMS);
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void signedUrlPathIsSlashWhenThereIsImplicitlyNoPathRelativeToTheBaseUrl() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/", CONTEXT_PATH, ALL_PARAMS);
        generateGet("https://example.com/base", "https://example.com/base", ALL_PARAMS);
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    @Ignore("AC-1799")
    public void canonicalRequestPreservesEncodedTargetPath() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/some%20path", "/", Collections.emptyMap());
        generateGet("https://example.com/some%20path", "https://example.com/", Collections.emptyMap());
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    @Ignore("AC-1799")
    public void canonicalRequestCorrectWithNoContextPath() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/some%20path", "", Collections.emptyMap());
        generateGet("https://example.com/some%20path", "https://example.com/", Collections.emptyMap());
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    @Ignore("AC-1799")
    public void canonicalRequestCorrectWithEncodedContextPath() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/some%20path", "/context%20path", Collections.emptyMap());
        generateGet("https://example.com/context%20path/some%20path", "https://example.com/context%20path", Collections.emptyMap());
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    @Ignore("AC-1799")
    public void canonicalRequestCorrectWithMultiSegmentContextPath() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String expectedQueryHash = generateQueryHash(HttpMethod.GET, "/some%20path", "/context/path", Collections.emptyMap());
        generateGet("https://example.com/context/path/some%20path", "https://example.com/context/path", Collections.emptyMap());
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Before
    public void beforeEachTest() {
        when(jwtService.issueJwt(any(String.class), eq(SECRET))).thenReturn(A_MOCK_JWT);
        when(consumerService.getConsumer()).thenReturn(Consumer.key("whatever").name("whatever").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build());

        UserProfile userProfile = mock(UserProfile.class);
        when(userManager.getRemoteUser()).thenReturn(userProfile);
        when(userProfile.getFullName()).thenReturn(USER_DISPLAY_NAME);
        when(userProfile.getUsername()).thenReturn(USER_NAME);
        when(userProfile.getUserKey()).thenReturn(new UserKey(USER_KEY));

        jwtBuilderFactory = new TestJwtJsonBuilderFactory(new SubjectJwtClaimWriter(userManager));
        generator = new JwtAuthorizationGenerator(jwtService, jwtBuilderFactory, constantSecretSupplier(SECRET), consumerService, A_URI_BASE);
        generate();
    }

    private Supplier<String> constantSecretSupplier(final String secret) {
        return () -> secret;
    }

    private ArgumentMatcher<String> hasQueryHash(String expectedQueryHash) {
        return hasJwtClaimWithValue(JwtConstants.Claims.QUERY_HASH, expectedQueryHash);
    }

    private String generateQueryHash(HttpMethod httpMethod, String targetUrl, String baseUrl, Map<String, String[]> params) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        CanonicalHttpUriRequest canonicalRequest = new CanonicalHttpUriRequest(httpMethod.toString(), targetUrl, baseUrl, params);
        return HttpRequestCanonicalizer.computeCanonicalRequestHash(canonicalRequest);
    }

    private Optional<String> generate() {
        return generator.generate(HttpMethod.POST, A_URI, PARAMS);
    }

    private String generateGet(String url, String baseUrl, Map<String, String[]> params) {
        return new JwtAuthorizationGenerator(jwtService, jwtBuilderFactory, constantSecretSupplier(SECRET), consumerService, URI.create(baseUrl)).generate(HttpMethod.GET, URI.create(url), params).get();
    }
}
