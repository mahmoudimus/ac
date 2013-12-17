package com.atlassian.plugin.connect.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.fugue.Option;
import com.atlassian.jwt.applinks.JwtService;
import com.atlassian.plugin.connect.spi.http.HttpMethod;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class JwtAuthorizationGenerator extends DefaultAuthorizationGeneratorBase
{
    private final JwtService jwtService;
    private final ApplicationLink applicationLink;

    public JwtAuthorizationGenerator(JwtService jwtService, ApplicationLink applicationLink)
    {
        this.jwtService = jwtService;
        this.applicationLink = applicationLink;
    }

    @Override
    public Option<String> generate(HttpMethod method, URI url, Map<String, List<String>> parameters)
    {
        String encodedJwt = "";
        return Option.some(encodedJwt);
    }
}
