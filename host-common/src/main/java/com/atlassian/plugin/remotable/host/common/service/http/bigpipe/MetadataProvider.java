package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import java.util.Map;

interface MetadataProvider
{
    Map<String, String> getMetadata();
}
