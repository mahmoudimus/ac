package com.atlassian.plugin.connect.plugin;

import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;

import java.net.URI;
import java.util.List;
import java.util.Map;

public abstract class DefaultAuthorizationGeneratorBase implements AuthorizationGenerator
{
    @Override
    @Deprecated
    public String generate(String method, URI url, Map<String, List<String>> parameters)
    {
        return generate(HttpMethod.valueOf(method), url, parameters).getOrNull();
    }
}
