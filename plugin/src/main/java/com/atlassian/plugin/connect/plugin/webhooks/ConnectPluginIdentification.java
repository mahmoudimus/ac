package com.atlassian.plugin.connect.plugin.webhooks;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.webhooks.api.register.listener.WebHookListenerRegistrationDetails;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class ConnectPluginIdentification
{
    private final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final LegacyAddOnIdentifierService legacyAddOnIdentifierService;

    @Autowired
    public ConnectPluginIdentification(final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService, final LegacyAddOnIdentifierService legacyAddOnIdentifierService)
    {
        this.jsonConnectAddOnIdentifierService = jsonConnectAddOnIdentifierService;
        this.legacyAddOnIdentifierService = legacyAddOnIdentifierService;
    }

    public Option<String> connectAddOnKey(WebHookListenerRegistrationDetails registrationDetails)
    {
        return registrationDetails.getModuleDescriptorDetails().map(new Function<WebHookListenerRegistrationDetails.ModuleDescriptorRegistrationDetails, String>()
        {
            @Override
            public String apply(final WebHookListenerRegistrationDetails.ModuleDescriptorRegistrationDetails registrationDetails)
            {
                return registrationDetails.getPluginKey();
            }
        }).filter(isConnectIdentifier);
    }

    private final Predicate<String> isConnectIdentifier = new Predicate<String>()
    {
        @Override
        public boolean apply(final String pluginKey)
        {
            return jsonConnectAddOnIdentifierService.isConnectAddOn(pluginKey) || legacyAddOnIdentifierService.isConnectAddOn(pluginKey);
        }
    };

}
