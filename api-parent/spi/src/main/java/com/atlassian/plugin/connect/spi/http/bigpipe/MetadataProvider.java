package com.atlassian.plugin.connect.spi.http.bigpipe;

import java.util.Map;

interface MetadataProvider
{
    Map<String, String> getMetadata();
}
