package com.atlassian.plugin.remotable.plugin.webhook;

public interface WebHookIdsAccessor
{
    Iterable<String> getIds();
}
