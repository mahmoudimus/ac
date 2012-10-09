package com.atlassian.labs.remoteapps.plugin.webhook;

public interface WebHookIdsAccessor
{
    Iterable<String> getIds();
}
