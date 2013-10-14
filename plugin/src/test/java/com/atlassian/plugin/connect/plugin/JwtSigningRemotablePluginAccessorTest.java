package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponseTransformation;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.license.LicenseStatus;
import com.atlassian.plugin.connect.plugin.util.LocaleHelper;
import com.atlassian.plugin.connect.plugin.util.http.CachingHttpContentRetriever;
import com.atlassian.plugin.connect.plugin.util.http.HttpContentRetriever;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.google.common.base.Objects;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JwtSigningRemotablePluginAccessorTest
{
    private static final String PLUGIN_KEY = "key";
    private static final String PLUGIN_NAME = "name";
    private static final String OUTGOING_FULL_GET_URL = "http://server:1234/basepath/path?param=param+value";
    private static final String INTERNAL_FULL_GET_URL = OUTGOING_FULL_GET_URL + "&lic=active&loc=whatever";
    private static final Map<String,String> GET_HEADERS = Collections.singletonMap("header", "header value");
    private static final Map<String,String> GET_PARAMS = Collections.singletonMap("param", "param value");
    private static final Map<String,String[]> GET_PARAMS_STRING_ARRAY = Collections.singletonMap("param", new String[]{"param value"});
    private static final URI GET_PATH = URI.create("/path");
    private static final String EXPECTED_GET_RESPONSE = "expected";
    private static final String BASE_URL = "http://server:1234/basepath";
    private static final String MOCK_JWT = "just.an.example";
    private @Mock JwtService jwtService;
    private @Mock ApplicationLink applicationLink;

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
    public void createdRemotePluginAccessorCorrectlyCallsTheHttpContentRetriever() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().executeAsync(HttpMethod.GET, GET_PATH, GET_PARAMS, GET_HEADERS).get(), is(EXPECTED_GET_RESPONSE));
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

    @Test
    public void authorizationGeneratorIsNotNull() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getAuthorizationGenerator(), is(not(nullValue())));
    }

    @Test
    public void issuedAtTimeIsClaimed()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasAnyIssuedAtTime()), any(ApplicationLink.class));
    }

    @Test
    public void issuedAtTimeClaimIsReasonable()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasIssuedAtTimeCloseToNow()), any(ApplicationLink.class));
    }

    @Test
    public void expirationTimeIsClaimed()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasAnyExpirationTime()), any(ApplicationLink.class));
    }

    @Test
    public void expirationTimeClaimIsReasonable()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasExpiresAtTimeCloseToDefaultWindowFromNow()), any(ApplicationLink.class));
    }

    @Test
    public void issuerIsClaimed()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasAnyIssuer()), any(ApplicationLink.class));
    }

    @Test
    public void issuerClaimIsCorrect()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasIssuer(applicationLink.getId().get())), any(ApplicationLink.class));
    }

    @Test
    public void queryHashIsClaimed()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasAnyQueryHash()), any(ApplicationLink.class));
    }

    @Test
    public void queryHashClaimIsCorrect() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        String expectedQueryHash = HttpRequestCanonicalizer.computeCanonicalRequestHash(new CanonicalHttpUriRequest(new HttpGet(OUTGOING_FULL_GET_URL), ""));
        verify(jwtService).issueJwt(argThat(hasQueryHash(expectedQueryHash)), any(ApplicationLink.class));
    }

    @Test
    public void thereAreNoUnexpectedClaims()
    {
        createRemotePluginAccessor().signGetUrl(GET_PATH, GET_PARAMS_STRING_ARRAY);
        verify(jwtService).issueJwt(argThat(hasExactlyTheseClaims("iss", "iat", "exp", JwtConstants.Claims.QUERY_HASH)), any(ApplicationLink.class));
    }

    private ArgumentMatcher<String> hasExactlyTheseClaims(String... claimNames)
    {
        return new ClaimNamesMatcher(claimNames);
    }

    private ArgumentMatcher<String> hasAnyIssuedAtTime()
    {
        return new StringClaimMatcher("iat");
    }

    private ArgumentMatcher<String> hasIssuedAtTimeCloseToNow()
    {
        return new LongClaimMatcher("iat", System.currentTimeMillis()/1000, 3); // issued within 3 seconds of now
    }

    private ArgumentMatcher<String> hasAnyExpirationTime()
    {
        return new StringClaimMatcher("exp");
    }

    private ArgumentMatcher<String> hasExpiresAtTimeCloseToDefaultWindowFromNow()
    {
        return new LongClaimMatcher("exp", System.currentTimeMillis()/1000 + 3 * 60, 3); // expires within 3 seconds of 3 minutes from now
    }

    private ArgumentMatcher<String> hasAnyQueryHash()
    {
        return new StringClaimMatcher(JwtConstants.Claims.QUERY_HASH);
    }

    private ArgumentMatcher<String> hasQueryHash(String queryHash)
    {
        return new StringClaimMatcher(JwtConstants.Claims.QUERY_HASH, queryHash);
    }

    private ArgumentMatcher<String> hasAnyIssuer()
    {
        return new StringClaimMatcher("iss");
    }

    private ArgumentMatcher<String> hasIssuer(String issuer)
    {
        return new StringClaimMatcher("iss", issuer);
    }

    private static class StringClaimMatcher extends ArgumentMatcher<String>
    {
        private final String claimName;
        private final String expectedClaimValue;
        private final boolean valueMatters;

        StringClaimMatcher(String claimName)
        {
            this.claimName = claimName;
            this.expectedClaimValue = null;
            this.valueMatters = false;
        }

        StringClaimMatcher(String claimName, String expectedClaimValue)
        {
            this.claimName = claimName;
            this.expectedClaimValue = expectedClaimValue;
            this.valueMatters = true;
        }

        @Override
        public boolean matches(Object argument)
        {
            assertThat(argument, is(instanceOf(String.class)));
            try
            {
                JSONObject jsonObject = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse((String) argument);
                boolean hasClaim = jsonObject.containsKey(claimName);
                return valueMatters ? hasClaim && Objects.equal(expectedClaimValue, jsonObject.get(claimName)) : hasClaim;
            }
            catch (ParseException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Expecting claim \"" + claimName + "\" to have value ");
            description.appendValue(expectedClaimValue);
        }
    }

    private static class LongClaimMatcher extends ArgumentMatcher<String>
    {
        private final String claimName;
        private final long expectedClaimValue;
        private final long tolerance;

        private LongClaimMatcher(String claimName, long expectedClaimValue, long tolerance)
        {
            this.claimName = claimName;
            this.expectedClaimValue = expectedClaimValue;
            this.tolerance = tolerance;
        }

        @Override
        public boolean matches(Object argument)
        {
            assertThat(argument, is(instanceOf(String.class)));
            try
            {
                JSONObject jsonObject = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse((String) argument);

                if (jsonObject.containsKey(claimName))
                {
                    Object actualClaimValue = jsonObject.get(claimName);
                    Long actualClaimLong = null;

                    if (actualClaimValue instanceof Number)
                    {
                        actualClaimLong = ((Number) actualClaimValue).longValue();
                    }
                    else if (null != actualClaimValue)
                    {
                        actualClaimLong = Long.parseLong(actualClaimValue.toString());
                    }

                    if (null != actualClaimLong)
                    {
                        return Math.abs(actualClaimLong - expectedClaimValue) <= tolerance;
                    }
                }

                return false;
            }
            catch (ParseException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText(String.format("Expecting claim \"%s\" to be within %d of %d", claimName, tolerance, expectedClaimValue));
        }
    }

    private static class ClaimNamesMatcher extends ArgumentMatcher<String>
    {
        private final Set<String> expectedClaimNames;

        private ClaimNamesMatcher(String... expectedClaimNames)
        {
            this.expectedClaimNames = new HashSet<String>(Arrays.asList(expectedClaimNames));
        }

        @Override
        public boolean matches(Object argument)
        {
            assertThat(argument, is(instanceOf(String.class)));
            try
            {
                JSONObject jsonObject = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse((String) argument);
                return jsonObject.keySet().equals(expectedClaimNames);
            }
            catch (ParseException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Expecting exactly these claims: ");
            description.appendValueList("[", ",", "]", expectedClaimNames);
        }
    }

    private RemotablePluginAccessor createRemotePluginAccessor()
    {
        when(applicationLink.getId()).thenReturn(new ApplicationId(UUID.randomUUID().toString()));
        when(jwtService.issueJwt(any(String.class), eq(applicationLink))).thenReturn(MOCK_JWT);
        ApplicationLinkAccessor applicationLinkAccessor = mock(ApplicationLinkAccessor.class);
        when(applicationLinkAccessor.getApplicationLink(PLUGIN_KEY)).thenReturn(applicationLink);
        Supplier<URI> baseUrlSupplier = new Supplier<URI>()
        {
            @Override
            public URI get()
            {
                return URI.create(BASE_URL);
            }
        };
        return new JwtSigningRemotablePluginAccessor(PLUGIN_KEY, PLUGIN_NAME, baseUrlSupplier, jwtService, applicationLinkAccessor, mockCachingHttpContentRetriever());
    }

    private HttpContentRetriever mockCachingHttpContentRetriever()
    {
        LicenseRetriever licenseRetriever = mock(LicenseRetriever.class);
        when(licenseRetriever.getLicenseStatus(PLUGIN_KEY)).thenReturn(LicenseStatus.ACTIVE);

        LocaleHelper localeHelper = mock(LocaleHelper.class);
        when(localeHelper.getLocaleTag()).thenReturn("whatever");

        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        HttpClient httpClient = mockHttpClient(mockRequest(EXPECTED_GET_RESPONSE));
        when(httpClientFactory.create(any(HttpClientOptions.class))).thenReturn(httpClient);

        return new CachingHttpContentRetriever(licenseRetriever, localeHelper, httpClientFactory, mock(PluginRetrievalService.class, RETURNS_DEEP_STUBS));
    }

    private HttpClient mockHttpClient(Request request)
    {
        HttpClient httpClient = mock(HttpClient.class, RETURNS_DEEP_STUBS);
        when(httpClient.newRequest(INTERNAL_FULL_GET_URL)).thenReturn(request);
        return httpClient;
    }

    private Request mockRequest(String promisedHttpResponse)
    {
        Request request = mock(Request.class);
        {
            when(request.setHeaders(GET_HEADERS)).thenReturn(request);
            when(request.setAttributes(any(Map.class))).thenReturn(request);
            {
                ResponseTransformation responseTransformation = mockResponseTransformation(promisedHttpResponse);
                ResponsePromise responsePromise = mock(ResponsePromise.class);
                when(responsePromise.transform()).thenReturn(responseTransformation);
                when(request.execute(any(Request.Method.class))).thenReturn(responsePromise);
            }
        }
        return request;
    }

    private ResponseTransformation mockResponseTransformation(String promisedHttpResponse)
    {
        ResponseTransformation responseTransformation = mock(ResponseTransformation.class);
        when(responseTransformation.ok(any(Function.class))).thenReturn(responseTransformation);
        when(responseTransformation.forbidden(any(Function.class))).thenReturn(responseTransformation);
        when(responseTransformation.others(any(Function.class))).thenReturn(responseTransformation);
        when(responseTransformation.fail(any(Function.class))).thenReturn(responseTransformation);
        {
            Promise<String> promise = mock(Promise.class);

            try
            {
                when(promise.get()).thenReturn(promisedHttpResponse);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            catch (ExecutionException e)
            {
                throw new RuntimeException(e);
            }

            when(responseTransformation.toPromise()).thenReturn(promise);
        }
        return responseTransformation;
    }
}
