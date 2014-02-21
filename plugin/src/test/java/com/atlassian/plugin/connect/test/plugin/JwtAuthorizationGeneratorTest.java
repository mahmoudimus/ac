package com.atlassian.plugin.connect.test.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.fugue.Option;
import com.atlassian.jira.security.auth.trustedapps.KeyFactory;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.JwtAuthorizationGenerator;
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
import java.util.Map;

import static com.atlassian.jwt.JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class JwtAuthorizationGeneratorTest
{
    private static final String A_MOCK_JWT = "a.mock.jwt";
    private static final Map<String, String[]> PARAMS = ImmutableMap.of("a_param", new String[]{"a_value"});
    private static final URI A_URI = URI.create("http://any.url/path?b_param=b+value+with+spaces&c_param=c_value");

    @Mock
    private JwtService jwtService;
    @Mock
    private ApplicationLink applicationLink;
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
        verify(jwtService).issueJwt(argThat(hasClaim("iss")), eq(applicationLink));
    }

    @Test
    public void hasIssuedAtClaim()
    {
        verify(jwtService).issueJwt(argThat(hasClaim("iat")), eq(applicationLink));
    }

    @Test
    public void hasExpiresAtClaim()
    {
        verify(jwtService).issueJwt(argThat(hasClaim("exp")), eq(applicationLink));
    }

    @Test
    public void hasNoSubjectClaim()
    {
        verify(jwtService).issueJwt(argThat(hasNoSuchClaim("sub")), eq(applicationLink));
    }

    @Test
    public void hasQueryHashClaim()
    {
        verify(jwtService).issueJwt(argThat(hasClaim(JwtConstants.Claims.QUERY_HASH)), eq(applicationLink));
    }

    @Test
    public void hasQueryHashClaimWithCorrectValue() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        CanonicalHttpUriRequest canonicalRequest = new CanonicalHttpUriRequest(HttpMethod.POST.toString(), A_URI.getPath(), "",
                ImmutableMap.of("a_param", new String[]{"a_value"}, "b_param", new String[]{"b value with spaces"}, "c_param", new String[]{"c_value"}));
        String expectedQueryHash = HttpRequestCanonicalizer.computeCanonicalRequestHash(canonicalRequest);
        verify(jwtService).issueJwt(argThat(hasClaim(JwtConstants.Claims.QUERY_HASH, expectedQueryHash, true)), eq(applicationLink));
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

    @Before
    public void beforeEachTest()
    {
        when(jwtService.issueJwt(any(String.class), eq(applicationLink))).thenReturn(A_MOCK_JWT);
        when(consumerService.getConsumer()).thenReturn(Consumer.key("whatever").name("whatever").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).publicKey(new KeyFactory.InvalidPublicKey(new Exception())).build());
        generator = new JwtAuthorizationGenerator(jwtService, applicationLink, consumerService);
        generate();
    }

    private Option<String> generate()
    {
        return generator.generate(HttpMethod.POST, A_URI, PARAMS);
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
