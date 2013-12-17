package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.fugue.Option;
import com.atlassian.jira.security.auth.trustedapps.KeyFactory;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.jwt.core.JwtUtil;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
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
    private static final String A_MOCK_JWT = "a.mock.jwt";
    private static final Map<String,List<String>> PARAMS = ImmutableMap.of("name", asList("value"));
    private static final URI A_URI = URI.create("http://any.url/path");

    @Mock private JwtService jwtService;
    @Mock private ApplicationLink applicationLink;
    @Mock private ConsumerService consumerService;

    private AuthorizationGenerator generator;

    @Test
    public void authorizationHeaderContainsJwt()
    {
        assertThat(generate(), is(Option.some(JwtUtil.JWT_AUTH_HEADER_PREFIX + A_MOCK_JWT)));
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
        return new ArgumentMatcher<String>()
        {
            @Override
            public boolean matches(Object actual)
            {
                return actual instanceof String
                        && !StringUtils.isEmpty((String) actual)
                        && new JsonParser().parse((String)actual).getAsJsonObject().has(claimName);
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
        };
    }
}
