package com.atlassian.plugin.connect.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.spi.http.HttpMethod;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class JwtAuthorizationGenerator extends DefaultAuthorizationGeneratorBase
{
    @Override
    public Option<String> generate(HttpMethod method, URI url, Map<String, List<String>> parameters)
    {
        return Option.option(""); // this appears to be needed only for adding an OAuth1 WWW-Authenticate or Authentication header
    }
}
