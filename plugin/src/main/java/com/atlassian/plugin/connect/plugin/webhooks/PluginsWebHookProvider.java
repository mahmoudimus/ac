package com.atlassian.plugin.connect.plugin.webhooks;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.beans.XmlDescriptorCodeInvokedEventBean;
import com.atlassian.plugin.connect.modules.gson.JiraConfluenceConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.capabilities.event.ConnectAddonEventSerializer;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder;
import com.atlassian.plugin.connect.spi.event.*;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.spi.provider.*;

import javax.inject.Named;

/**
 * Registers Web hooks for remote plugins
 */
@ExportAsService
@Named
public final class PluginsWebHookProvider implements WebHookProvider
{
    public static final String REMOTE_PLUGIN_INSTALLED = "remote_plugin_installed";
    public static final String REMOTE_PLUGIN_ENABLED = "remote_plugin_enabled";
    public static final String REMOTE_PLUGIN_DISABLED = "remote_plugin_disabled";

    public static final String CONNECT_ADDON_ENABLED = "connect_addon_enabled";
    public static final String CONNECT_ADDON_DISABLED = "connect_addon_disabled";
    public static final String CONNECT_ADDON_UNINSTALLED = "connect_addon_uninstalled";

    @Override
    public void provide(WebHookRegistrar registrar)
    {
        /* Legacy XML Stuff */
        provideLegacyXmlSerializers(registrar);
        
        /* New Json Stuff */
        final EventSerializerFactory connectAddonEventSerializerFactory = new ConnectAddonEventSerializerFactory();
        final ConnectAddonEventMatcher connectAddonEventMatcher = new ConnectAddonEventMatcher();
        registrar.webhook(CONNECT_ADDON_ENABLED).whenFired(ConnectAddonEnabledEvent.class).matchedBy(connectAddonEventMatcher).serializedWith(connectAddonEventSerializerFactory);
        registrar.webhook(CONNECT_ADDON_DISABLED).whenFired(ConnectAddonDisabledEvent.class).matchedBy(connectAddonEventMatcher).serializedWith(connectAddonEventSerializerFactory);
        //registrar.webhook(CONNECT_ADDON_UNINSTALLED).whenFired(ConnectAddonUninstalledEvent.class).matchedBy(connectAddonEventMatcher).serializedWith(connectAddonEventSerializerFactory);

        // XML descriptor code invocations
        registrar.webhook("connect.xmldescriptor.code.invoked").whenFired(XmlDescriptorCodeInvokedEvent.class).matchedBy(new XmlDescriptorCodeInvokedEventMatcher()).serializedWith(new XmlDescriptorCodeInvokedEventSerializerFactory());
    }

    @XmlDescriptor
    private static void provideLegacyXmlSerializers(WebHookRegistrar registrar)
    {
        XmlDescriptorExploder.notifyAndExplode(null);

        final EventSerializerFactory serializerFactory = new RemotePluginEventSerializerFactory();
        final RemotePluginEventMatcher eventTypeMatcher = new RemotePluginEventMatcher();

        registrar.webhook(REMOTE_PLUGIN_INSTALLED).whenFired(RemotePluginInstalledEvent.class).matchedBy(eventTypeMatcher).serializedWith(serializerFactory);
        registrar.webhook(REMOTE_PLUGIN_ENABLED).whenFired(RemotePluginEnabledEvent.class).matchedBy(eventTypeMatcher).serializedWith(serializerFactory);
        registrar.webhook(REMOTE_PLUGIN_DISABLED).whenFired(RemotePluginDisabledEvent.class).matchedBy(eventTypeMatcher).serializedWith(serializerFactory);
    }

    private static final class RemotePluginEventMatcher<E extends RemotePluginEvent> implements EventMatcher<E>
    {
        @Override
        public boolean matches(final E event, final Object consumerParams)
        {
            return consumerParams instanceof PluginModuleListenerParameters
                    && ((PluginModuleListenerParameters) consumerParams).getPluginKey().equals(event.getPluginKey());
        }
    }

    private static final class RemotePluginEventSerializerFactory<E extends RemotePluginEvent> implements EventSerializerFactory<E>
    {
        @Override
        public EventSerializer create(final E event)
        {
            return EventSerializers.forMap(event, event.toMap());
        }
    }

    private static final class ConnectAddonEventMatcher<E extends ConnectAddonLifecycleEvent> implements EventMatcher<E>
    {
        @Override
        public boolean matches(final E event, final Object consumerParams)
        {
            return consumerParams instanceof PluginModuleListenerParameters
                    && ((PluginModuleListenerParameters) consumerParams).getPluginKey().equals(event.getPluginKey());
        }
    }

    private static final class ConnectAddonEventSerializerFactory<E extends ConnectAddonLifecycleEvent> implements EventSerializerFactory<E>
    {
        @Override
        public EventSerializer create(final E event)
        {
            String data = null;
            if (event instanceof ConnectAddonLifecycleWithDataEvent)
            {
                data = ((ConnectAddonLifecycleWithDataEvent) event).getData();
            }
            return new ConnectAddonEventSerializer(event, data);
        }
    }

    private static final class XmlDescriptorCodeInvokedEventMatcher implements EventMatcher<XmlDescriptorCodeInvokedEvent>
    {
        @Override
        public boolean matches(XmlDescriptorCodeInvokedEvent event, Object consumerParams)
        {
            return consumerParams instanceof PluginModuleListenerParameters
                    && ((PluginModuleListenerParameters) consumerParams).getPluginKey().equals(event.getAddOnKey());
        }
    }

    private static final class XmlDescriptorCodeInvokedEventSerializerFactory implements EventSerializerFactory<XmlDescriptorCodeInvokedEvent>
    {
        @Override
        public EventSerializer create(XmlDescriptorCodeInvokedEvent event)
        {
            return new XmlDescriptorCodeInvokedEventSerializer(event);
        }
    }

    private static final class XmlDescriptorCodeInvokedEventSerializer implements EventSerializer
    {
        private final XmlDescriptorCodeInvokedEvent event;

        private XmlDescriptorCodeInvokedEventSerializer(XmlDescriptorCodeInvokedEvent event)
        {
            this.event = event;
        }

        @Override
        public Object getEvent()
        {
            return event;
        }

        @Override
        public String getWebHookBody() throws EventSerializationException
        {
            return JiraConfluenceConnectModulesGsonFactory.getGsonBuilder().setPrettyPrinting().create().toJson(new XmlDescriptorCodeInvokedEventBean(event.getAddOnKey(), event.getStackTrace()));
        }
    }
}
