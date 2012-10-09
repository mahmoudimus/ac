package com.atlassian.labs.remoteapps.spi.webhook;

/**
 * The fluent interface starting point for registering a web hook
 */
public interface WebHookRegistrar
{
    EventBuilder webhook(String id);
}
