package com.atlassian.labs.remoteapps.spi.webhook;

/**
 * Provides multiple web hooks via registering with the registrar.
 */
public interface WebHookProvider
{
    void provide(WebHookRegistrar registrar);
}
