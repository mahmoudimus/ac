package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.JwtService;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilderFactory;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.applinks.DefaultConnectApplinkManager;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.util.auth.TestJwtJsonBuilderFactory;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.atlassian.jwt.JwtConstants.HttpRequests.AUTHORIZATION_HEADER;
import static com.atlassian.jwt.JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtClaimLongMatcher.hasJwtClaimWithLongValue;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtClaimMatcher.hasJwtClaim;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtClaimMatcher.hasJwtClaimWithValue;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtClaimsetMatcher.hasJwtClaimset;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtContextClaimMatcher.hasJwtContextKey;
import static com.atlassian.plugin.connect.plugin.util.matcher.JwtContextClaimMatcher.hasJwtContextKeyWithValue;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@ConvertToWiredTest
@RunWith (MockitoJUnitRunner.class)
public class JwtSigningRemotablePluginAccessorTest extends BaseSigningRemotablePluginAccessorTest
{
    protected static final String MOCK_JWT = "just.an.example";
    private static final String CONSUMER_KEY = "12345-abcde-09876-zyxwv";

    private static final String USER_KEY = "MrFreeze";
    private static final String USER_NAME = "vfries";
    private static final String USER_DISPLAY_NAME = "Dr. Victor Fries";

    private static final Map<String, String[]> GET_PARAMS_STRING_ARRAY = Collections.singletonMap("param", new String[] { "param value" });
    private static final URI FULL_PATH_URI = URI.create(FULL_PATH_URL);
    private static final URI FULL_PATH_URI_WITH_PARAM = URI.create(FULL_PATH_URL + "?param=param%20value");
    private static final URI GET_PATH = URI.create("/path");
    private static final URI UNEXPECTED_ABSOLUTE_URI = URI.create("http://www.example.com/path");
    private static final String SECRET = "secret";

    private
    @Mock
    JwtService jwtService;
    private
    @Mock
    ApplicationLink applicationLink;

    @Test
    public void createdRemotePluginAccessorHasCorrectPluginKey() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getKey(), is(PLUGIN_KEY));
    }

    @Test
    public void createdRemotePluginAccessorHasCorrectPluginName() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getName(), is(PLUGIN_NAME));
    }

    @Test
    public void createdRemotePluginAccessorCorrectlyCallsTheHttpContentRetriever()
            throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().executeAsync(HttpMethod.GET, GET_PATH, GET_PARAMS_STRING_ARRAY, UNAUTHED_GET_HEADERS).get(), is(EXPECTED_GET_RESPONSE));
    }

    @Test
    public void createdRemotePluginAccessorCorrectlySignsTheRequestUrl() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY), is(OUTGOING_FULL_GET_URL + "&jwt=" + MOCK_JWT));
    }

    @Test
    public void createdRemotePluginAccessorHasCorrectBaseUrl() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getBaseUrl().toString(), is(BASE_URL));
    }

    @Test
    public void createdRemotePluginAccessorCreatesCorrectGetUrl() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().createGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY), is(OUTGOING_FULL_GET_URL));
    }

    @Test (expected = IllegalArgumentException.class)
    public void createdRemotePluginAccessorThrowsIAEWhenGetUrlIsIncorrectlyAbsolute()
            throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().createGetUrl(UNEXPECTED_ABSOLUTE_URI, GET_PARAMS_STRING_ARRAY), is(OUTGOING_FULL_GET_URL));
    }

    @Test (expected = IllegalArgumentException.class)
    public void createdRemotePluginAccessorThrowsIAEWhenGetUrlIsAbsoluteToAddon()
            throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().createGetUrl(FULL_PATH_URI, GET_PARAMS_STRING_ARRAY), is(OUTGOING_FULL_GET_URL));
    }

    @Test (expected = IllegalArgumentException.class)
    public void createdRemotePluginAccessorThrowsIAEWhenDuplicateParams()
            throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().createGetUrl(FULL_PATH_URI_WITH_PARAM, GET_PARAMS_STRING_ARRAY), is(OUTGOING_FULL_GET_URL));
    }

    @Test
    public void authorizationGeneratorIsNotNull() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getAuthorizationGenerator(), is(not(nullValue())));
    }

    @Test
    public void issuedAtTimeIsClaimed()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasAnyIssuedAtTime()), eq(SECRET));
    }

    @Test
    public void issuedAtTimeClaimIsReasonable()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasIssuedAtTimeCloseToNow()), eq(SECRET));
    }

    @Test
    public void expirationTimeIsClaimed()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasAnyExpirationTime()), eq(SECRET));
    }

    @Test
    public void expirationTimeClaimIsReasonable()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasExpiresAtTimeCloseToDefaultWindowFromNow()), eq(SECRET));
    }

    @Test
    public void issuerIsClaimed()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasAnyIssuer()), eq(SECRET));
    }

    @Test
    public void issuerClaimIsCorrect()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasIssuer(CONSUMER_KEY)), eq(SECRET));
    }

    @Test
    public void subjectIsClaimed()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasAnySubject()), eq(SECRET));
    }

    @Test
    public void subjectClaimIsCorrect()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasSubject(USER_KEY)), eq(SECRET));
    }

    @Test
    public void queryHashIsClaimed()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasAnyQueryHash()), eq(SECRET));
    }

    @Test
    public void queryHashClaimIsCorrect() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);

        CanonicalHttpUriRequest request = new CanonicalHttpUriRequest(HttpMethod.GET.toString(), URI.create(OUTGOING_FULL_GET_URL).getPath(), CONTEXT_PATH, GET_PARAMS_STRING_ARRAY);
        String expectedQueryHash = HttpRequestCanonicalizer.computeCanonicalRequestHash(request);

        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), eq(SECRET));
    }

    @Test
    public void customContextClaimPresent()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasContextClaim()), eq(SECRET));
    }

    @Test
    public void customContextUserObjectPresent()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasJwtContextKey("user")), eq(SECRET));
    }

    @Test
    public void customContextUserKeyIsCorrect()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasJwtContextKeyWithValue("user.userKey", USER_KEY)), eq(SECRET));
    }

    @Test
    public void customContextUsernameIsCorrect()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasJwtContextKeyWithValue("user.username", USER_NAME)), eq(SECRET));
    }

    @Test
    public void customContextDisplayNameIsCorrect()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasJwtContextKeyWithValue("user.displayName", USER_DISPLAY_NAME)), eq(SECRET));
    }

    @Test
    public void hasContextClaimWhenAnonymous()
    {
        UserManager userManager = mock(UserManager.class);
        when(userManager.getRemoteUser()).thenReturn(null);

        createRemotePluginAccessor(userManager).signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);

        verify(jwtService).issueJwt(argThat(hasContextClaim()), eq(SECRET));
    }

    @Test
    public void customContextUserNotPresentIfAnonymous()
    {
        UserManager userManager = mock(UserManager.class);
        when(userManager.getRemoteUser()).thenReturn(null);

        createRemotePluginAccessor(userManager).signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);

        verify(jwtService).issueJwt(argThat(not(hasJwtContextKey("user"))), eq(SECRET));
    }

    @Test
    public void thereAreNoUnexpectedClaims()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasExactlyTheseClaims("iss", "sub", "iat", "exp", "context", JwtConstants.Claims.QUERY_HASH)), eq(SECRET));
    }

    @Test
    public void slashesAreNormalizedOnConcatenation() throws Exception
    {
        RemotablePluginAccessor accessor = createRemotePluginAccessor("https://example.com/addon/");
        assertThat(accessor.createGetUrl(URI.create("/handler"), Collections.<String, String[]>emptyMap()), is("https://example.com/addon/handler"));
    }

    @Test
    public void trailingSlashesAreLeftIntact() throws Exception
    {
        RemotablePluginAccessor accessor = createRemotePluginAccessor("https://example.com/addon");
        assertThat(accessor.createGetUrl(URI.create("/handler/"), Collections.<String, String[]>emptyMap()), is("https://example.com/addon/handler/"));
    }

    @Override
    protected Map<String, String> getPostSigningHeaders(Map<String, String> preSigningHeaders)
    {
        Map<String, String> headers = new HashMap<String, String>(preSigningHeaders);
        headers.put(AUTHORIZATION_HEADER, JWT_AUTH_HEADER_PREFIX + MOCK_JWT);
        return headers;
    }

    private ArgumentMatcher<String> hasExactlyTheseClaims(String... claimNames)
    {
        return hasJwtClaimset(claimNames);
    }

    private ArgumentMatcher<String> hasContextClaim()
    {
        return hasJwtClaim("context");
    }

    private ArgumentMatcher<String> hasAnyIssuedAtTime()
    {
        return hasJwtClaim("iat");
    }

    private ArgumentMatcher<String> hasIssuedAtTimeCloseToNow()
    {
        return hasJwtClaimWithLongValue("iat", System.currentTimeMillis() / 1000, 3); // issued within 3 seconds of now
    }

    private ArgumentMatcher<String> hasAnyExpirationTime()
    {
        return hasJwtClaim("exp");
    }

    private ArgumentMatcher<String> hasExpiresAtTimeCloseToDefaultWindowFromNow()
    {
        return hasJwtClaimWithLongValue("exp", System.currentTimeMillis() / 1000 + 3 * 60, 3); // expires within 3 seconds of 3 minutes from now
    }

    private ArgumentMatcher<String> hasAnyQueryHash()
    {
        return hasJwtClaim(JwtConstants.Claims.QUERY_HASH);
    }

    private ArgumentMatcher<String> hasQueryHash(String queryHash)
    {
        return hasJwtClaimWithValue(JwtConstants.Claims.QUERY_HASH, queryHash);
    }

    private ArgumentMatcher<String> hasAnyIssuer()
    {
        return hasJwtClaim("iss");
    }

    private ArgumentMatcher<String> hasIssuer(String issuer)
    {
        return hasJwtClaimWithValue("iss", issuer);
    }

    private ArgumentMatcher<String> hasAnySubject()
    {
        return hasJwtClaim("sub");
    }

    private ArgumentMatcher<String> hasSubject(String sub)
    {
        return hasJwtClaimWithValue("sub", sub);
    }

    private RemotablePluginAccessor createRemotePluginAccessor()
    {
        return createRemotePluginAccessor(BASE_URL, null);
    }

    private RemotablePluginAccessor createRemotePluginAccessor(final String baseUrl)
    {
        return createRemotePluginAccessor(baseUrl, null);
    }

    private RemotablePluginAccessor createRemotePluginAccessor(UserManager userManager)
    {
        return createRemotePluginAccessor(BASE_URL, userManager);
    }

    private RemotablePluginAccessor createRemotePluginAccessor(final String baseUrl, UserManager userManager)
    {
        when(jwtService.issueJwt(any(String.class), eq(SECRET))).thenReturn(MOCK_JWT);
        ConnectApplinkManager connectApplinkManager = mock(DefaultConnectApplinkManager.class);
        when(connectApplinkManager.getAppLink(PLUGIN_KEY)).thenReturn(applicationLink);
        when(applicationLink.getProperty(JwtConstants.AppLinks.SHARED_SECRET_PROPERTY_NAME)).thenReturn(SECRET);
        Supplier<URI> baseUrlSupplier = new Supplier<URI>()
        {
            @Override
            public URI get()
            {
                return URI.create(baseUrl);
            }
        };

        ConsumerService consumerService = mock(ConsumerService.class);
        Consumer consumer = new Consumer.InstanceBuilder(CONSUMER_KEY)
                .name("JIRA")
                .signatureMethod(Consumer.SignatureMethod.HMAC_SHA1)
                .build();
        when(consumerService.getConsumer()).thenReturn(consumer);

        if (userManager == null)
        {
            UserKey mockRemoteUserKey = new UserKey(USER_KEY);

            UserProfile mockRemoteUser = mock(UserProfile.class);
            when(mockRemoteUser.getUsername()).thenReturn(USER_NAME);
            when(mockRemoteUser.getFullName()).thenReturn(USER_DISPLAY_NAME);
            when(mockRemoteUser.getUserKey()).thenReturn(mockRemoteUserKey);

            userManager = mock(UserManager.class);
            when(userManager.getRemoteUser()).thenReturn(mockRemoteUser);
        }

        ConnectAddonBean addon = ConnectAddonBean.newConnectAddonBean()
                .withKey(PLUGIN_KEY)
                .withName(PLUGIN_NAME)
                .withBaseurl(baseUrl)
                .build();

        // create a JwtJsonBuilder that injects context.user into the JWT token
        JwtJsonBuilderFactory jwtBuilderFactory = new TestJwtJsonBuilderFactory(new SubjectJwtClaimWriter(userManager));

        return new JwtSigningRemotablePluginAccessor(addon, baseUrlSupplier, jwtBuilderFactory, jwtService, consumerService,
                connectApplinkManager, mockCachingHttpContentRetriever());
    }
}
