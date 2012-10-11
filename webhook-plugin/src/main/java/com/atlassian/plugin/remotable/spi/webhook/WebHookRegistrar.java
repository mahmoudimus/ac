package com.atlassian.plugin.remotable.spi.webhook;

/**
 * The fluent interface starting point for registering a web hook
 */
public interface WebHookRegistrar
{
    EventBuilder webhook(String id);
}
