package com.atlassian.plugin.connect.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.JwtUtil;
import com.atlassian.jwt.core.writer.NimbusJwtWriter;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.crypto.MACSigner;
import net.minidev.json.JSONObject;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JwtAuthorizationGeneratorTest
{

    private static final NimbusJwtWriter JWT_WRITER = new NimbusJwtWriter(SigningAlgorithm.HS256, new MACSigner("shared secret"));
    private static final String JSON = JSONObject.toJSONString(ImmutableMap.<String, String>of());

    @Test
    public void isCorrectWithUserKeyInUri()
    {
        assertThat(new JwtAuthorizationGenerator().generate(HttpMethod.POST, URI.create("http://any.url/path?user_key=some_user"), Collections.<String, List<String>>emptyMap()),
                is(Option.some(JwtUtil.JWT_AUTH_HEADER_PREFIX + JWT_WRITER.jsonToJwt(JSON))));
    }

    @Test
    public void isCorrectWithUserKeyInParams()
    {
        assertThat(new JwtAuthorizationGenerator().generate(HttpMethod.POST, URI.create("http://any.url/path"), ImmutableMap.<String, List< String >>of("user_id", asList("some_user"))), is(Option.some("")));
    }

    @Test
    public void isCorrectWithoutUserKey()
    {
        assertThat(new JwtAuthorizationGenerator().generate(HttpMethod.GET, URI.create("http://any.url"), Collections.<String, List<String>>emptyMap()), is(Option.some("")));
    }

    @Test
    public void handlesNullArguments()
    {
        assertThat(new JwtAuthorizationGenerator().generate((HttpMethod)null, null, null), is(Option.none(String.class)));
    }
}
