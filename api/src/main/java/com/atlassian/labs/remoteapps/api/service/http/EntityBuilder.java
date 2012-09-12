package com.atlassian.labs.remoteapps.api.service.http;

import java.io.InputStream;
import java.util.Map;

public interface EntityBuilder
{
    public Map<String, String> getHeaders();

    public InputStream build();
}
