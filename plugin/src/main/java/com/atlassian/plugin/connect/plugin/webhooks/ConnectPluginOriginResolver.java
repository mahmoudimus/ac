package com.atlassian.plugin.connect.plugin.webhooks;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.webhooks.api.register.listener.WebHookListenerRegistrationDetails;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonKeyOnly;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;

@Component
public final class ConnectPluginOriginResolver
{
    private final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final LegacyAddOnIdentifierService legacyAddOnIdentifierService;

    @Autowired
    public ConnectPluginOriginResolver(final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService, final LegacyAddOnIdentifierService legacyAddOnIdentifierService)
    {
        this.jsonConnectAddOnIdentifierService = jsonConnectAddOnIdentifierService;
        this.legacyAddOnIdentifierService = legacyAddOnIdentifierService;
    }

    public Option<String> connectAddOnKey(WebHookListenerRegistrationDetails registrationDetails)
    {
        return registrationDetails.getModuleDescriptorDetails().map(new Function<WebHookListenerRegistrationDetails.ModuleDescriptorRegistrationDetails, Option<String>>()
        {
            @Override
            public Option<String> apply(final WebHookListenerRegistrationDetails.ModuleDescriptorRegistrationDetails origin)
            {
                return option(tryFind(newArrayList(origin.getPluginKey(), origin.getModuleKey(), addonKeyOnly(origin.getModuleKey())), new Predicate<String>()
                {
                    @Override
                    public boolean apply(final String key)
                    {
                        return isConnectAddonIdentifier(key);
                    }
                }).orNull());
            }
        }).getOrElse(Option.<String>none());
    }

    private boolean isConnectAddonIdentifier(final String id)
    {
        return jsonConnectAddOnIdentifierService.isConnectAddOn(id) || legacyAddOnIdentifierService.isConnectAddOn(id);
    }
}
