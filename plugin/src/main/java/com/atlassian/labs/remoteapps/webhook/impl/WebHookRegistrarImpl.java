package com.atlassian.labs.remoteapps.webhook.impl;

import com.atlassian.labs.remoteapps.webhook.WebHookRegistration;
import com.atlassian.labs.remoteapps.webhook.external.EventBuilder;
import com.atlassian.labs.remoteapps.webhook.external.WebHookRegistrar;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class WebHookRegistrarImpl implements WebHookRegistrar
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
