package com.atlassian.plugin.connect.plugin.webhooks;

import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.beans.XmlDescriptorCodeInvokedEventBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.spi.event.*;
import com.atlassian.plugin.connect.spi.event.product.*;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.webhooks.api.register.WebHookEventGroup;
import com.atlassian.webhooks.api.register.WebHookPluginRegistration;
import com.atlassian.webhooks.api.register.listener.WebHookListener;
import com.atlassian.webhooks.api.util.EventSerializers;
import com.atlassian.webhooks.spi.*;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import static com.atlassian.webhooks.api.register.RegisteredWebHookEvent.withId;
import static com.atlassian.webhooks.api.register.WebHookEventSection.section;
import static com.atlassian.webhooks.api.register.listener.WebHookListenerRegistrationDetails.ModuleDescriptorRegistrationDetails;
import static com.atlassian.webhooks.api.util.EventMatchers.ALWAYS_TRUE;
import static com.google.common.base.Strings.nullToEmpty;

public final class ConnectWebHookPluginRegistrationFactory implements WebHookPluginRegistrationFactory
{
    private static final Gson GSON = ConnectModulesGsonFactory.getGsonBuilder().setPrettyPrinting().create();

    public static final String CONNECT_ADDON_ENABLED = "connect_addon_enabled";
    public static final String CONNECT_ADDON_DISABLED = "connect_addon_disabled";

    @XmlDescriptor
    public static final String XML_DESCRIPTOR_CODE_INVOKED = "connect.xmldescriptor.code.invoked";

    public static final String SERVER_UPGRADED = "server_upgraded";
    public static final String PLUGINS_UPGRADED = "plugins_upgraded";

    private final ApplicationProperties applicationProperties;
    private final ConsumerService consumerService;

    public ConnectWebHookPluginRegistrationFactory(final ApplicationProperties applicationProperties, final ConsumerService consumerService)
    {
        this.applicationProperties = applicationProperties;
        this.consumerService = consumerService;
    }

    private static final EventMatcher<PluginRelatedEvent> PLUGIN_MATCHER = new EventMatcher<PluginRelatedEvent>()
    {
        @Override
        public boolean matches(final PluginRelatedEvent event, final WebHookListener listener)
        {
            return listener.getRegistrationDetails().getModuleDescriptorDetails().fold(new Supplier<Boolean>()
            {
                @Override
                public Boolean get()
                {
                    return Boolean.FALSE;
                }
            }, new Function<ModuleDescriptorRegistrationDetails, Boolean>()
            {
                @Override
                public Boolean apply(final ModuleDescriptorRegistrationDetails registrationDetails)
                {
                    return registrationDetails.getPluginKey().equals(event.getPluginKey());
                }
            });
        }
    };

    @Override
    public WebHookPluginRegistration createPluginRegistration()
    {
        return WebHookPluginRegistration.builder()
                .addWebHookSection(section("connect-events")
                        .addGroup(addOnEvents())
                        .addGroup(upgradeEvents())
                        .addGroup(otherEvents())
                        .build())
                .eventSerializer(RemotePluginEvent.class, new EventSerializer<RemotePluginEvent>()
                {
                    @Override
                    public String serialize(final RemotePluginEvent event)
                    {
                        return EventSerializers.objectToJson(event.toMap());
                    }
                })
                .eventSerializer(ConnectAddonLifecycleWithDataEvent.class, new EventSerializer<ConnectAddonLifecycleWithDataEvent>()
                {
                    @Override
                    public String serialize(final ConnectAddonLifecycleWithDataEvent event)
                    {
                        return event.getData();
                    }
                })
                .eventSerializer(XmlDescriptorCodeInvokedEvent.class, new EventSerializer<XmlDescriptorCodeInvokedEvent>()
                {
                    @Override
                    public String serialize(final XmlDescriptorCodeInvokedEvent event)
                    {
                        return GSON.toJson(new XmlDescriptorCodeInvokedEventBean(event.getAddOnKey(), event.getStackTrace()));
                    }
                })
                .eventSerializer(UpgradedEvent.class, new EventSerializer<UpgradedEvent>()
                {
                    @Override
                    public String serialize(final UpgradedEvent event)
                    {
                        return EventSerializers.objectToJson(ImmutableMap.<String, Object>builder()
                                .put("key", consumerService.getConsumer().getKey())
                                .put("baseUrl", nullToEmpty(applicationProperties.getBaseUrl()))
                                .put("oldVersion", event.getOldVersion())
                                .put("newVersion", event.getNewVersion())
                                .build());
                    }
                })
                .build();
    }

    private WebHookEventGroup addOnEvents()
    {
        return WebHookEventGroup.builder()
                .addEvent(withId(CONNECT_ADDON_ENABLED).firedWhen(ConnectAddonEnabledEvent.class).isMatchedBy(PLUGIN_MATCHER))
                .addEvent(withId(CONNECT_ADDON_DISABLED).firedWhen(ConnectAddonDisabledEvent.class).isMatchedBy(PLUGIN_MATCHER))
                .build();
    }

    private WebHookEventGroup upgradeEvents()
    {
        return WebHookEventGroup.builder()
                .addEvent(withId(SERVER_UPGRADED).firedWhen(ServerUpgradedEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId(PLUGINS_UPGRADED).firedWhen(PluginsUpgradedEvent.class).isMatchedBy(ALWAYS_TRUE))
                .build();
    }

    private WebHookEventGroup otherEvents()
    {
        return WebHookEventGroup.builder()
                .addEvent(withId(XML_DESCRIPTOR_CODE_INVOKED).firedWhen(XmlDescriptorCodeInvokedEvent.class).isMatchedBy(PLUGIN_MATCHER))
                .build();
    }
}
