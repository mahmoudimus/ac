package com.atlassian.labs.remoteapps.plugin.webhook.impl;

import com.atlassian.labs.remoteapps.plugin.webhook.WebHookRegistration;
import com.atlassian.labs.remoteapps.spi.webhook.EventBuilder;
import com.atlassian.labs.remoteapps.spi.webhook.WebHookRegistrar;

import java.util.Set;

import static com.google.common.collect.Sets.*;

public final class WebHookRegistrarImpl implements WebHookRegistrar
{
    private final Set<WebHookRegistration> registrations = newHashSet();
    
    @Override
    public EventBuilder webhook(String id)
    {
        WebHookRegistration registration = new WebHookRegistration(id);
        registrations.add(registration);
        return new EventBuilderImpl(registration);
    }

    public Set<WebHookRegistration> getRegistrations()
    {
        return registrations;
    }
}
