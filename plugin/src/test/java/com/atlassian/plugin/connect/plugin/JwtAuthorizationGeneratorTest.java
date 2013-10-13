package com.atlassian.plugin.connect.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JwtAuthorizationGeneratorTest
{
    @Test
    public void generatesEmptyStringOption()
    {
        assertThat(new JwtAuthorizationGenerator().generate(HttpMethod.GET, URI.create("http://any.url"), Collections.<String, List<String>>emptyMap()), is(Option.none(String.class)));
    }

    @Test
    public void nullArgumentsDoNotMatter()
    {
        assertThat(new JwtAuthorizationGenerator().generate((HttpMethod)null, null, null), is(Option.none(String.class)));
    }
}
