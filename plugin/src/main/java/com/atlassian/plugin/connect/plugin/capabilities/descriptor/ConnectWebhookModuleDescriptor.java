package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebhookCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ModuleKeyGenerator;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;
import com.atlassian.webhooks.spi.provider.PluginModuleListenerParameters;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.net.URI;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectWebhookModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final ModuleDescriptorWebHookListenerRegistry webHookListenerRegistry;

    private final Plugin plugin;
    private final String pluginKey;
    private final WebhookCapabilityBean bean;
    private final Map<String, Object> moduleParams;
    private final String moduleKey;
    private final String completeKey;

    public ConnectWebhookModuleDescriptor(ModuleDescriptorWebHookListenerRegistry webHookListenerRegistry,
                                          WebhookCapabilityBean bean, Plugin plugin)
    {
        super(ModuleFactory.LEGACY_MODULE_FACTORY);
        this.bean = bean;
        this.webHookListenerRegistry = checkNotNull(webHookListenerRegistry);
        this.moduleParams = Maps.<String, Object>newHashMap(bean.getParams());
        this.moduleKey = ModuleKeyGenerator.generateKey("webhook");
        this.plugin = plugin;
        this.pluginKey = plugin.getKey();
        this.completeKey = buildCompleteKey(plugin, moduleKey);
    }

    @Override
    public Void getModule()
    {
        return null;
    }

    @Override
    public String getKey()
    {
        return moduleKey;
    }

    @Override
    public Plugin getPlugin()
    {
        return plugin;
    }

    @Override
    public String getCompleteKey()
    {
        return completeKey;
    }

    @Override
    public void enabled()
    {
        super.enabled();
        // TODO make sure URL is validated up the stack
        webHookListenerRegistry.register(bean.getEvent(), pluginKey, URI.create(bean.getUrl()),
                new PluginModuleListenerParameters(pluginKey,
                        Optional.of(moduleKey), moduleParams, bean.getEvent()));
    }

    @Override
    public void disabled()
    {
        // TODO make sure URL is validated up the stack
        webHookListenerRegistry.unregister(bean.getEvent(), pluginKey, URI.create(bean.getUrl()), new PluginModuleListenerParameters(pluginKey,
                Optional.of(moduleKey), moduleParams, bean.getEvent()));
        super.disabled();
    }

    /**
     * Copied from {@link AbstractModuleDescriptor} for the time being, should be made accessible.
     */
    private static String buildCompleteKey(final Plugin plugin, final String moduleKey)
    {
        if (plugin == null)
        {
            return null;
        }

        final StringBuffer completeKeyBuffer = new StringBuffer(32);
        completeKeyBuffer.append(plugin.getKey()).append(":").append(moduleKey);
        return completeKeyBuffer.toString();
    }
}
