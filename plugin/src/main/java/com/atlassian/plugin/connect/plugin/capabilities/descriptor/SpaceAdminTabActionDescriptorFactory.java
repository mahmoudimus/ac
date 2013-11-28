package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.confluence.event.events.plugin.XWorkStateChangeEvent;
import com.atlassian.confluence.plugin.descriptor.PluginAwareActionConfig;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceAdminIFrameAction;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceAdminTabContextInterceptor;
import com.atlassian.plugin.connect.plugin.module.page.SpaceAdminTabContext;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opensymphony.webwork.dispatcher.VelocityResult;
import com.opensymphony.xwork.ObjectFactory;
import com.opensymphony.xwork.config.Configuration;
import com.opensymphony.xwork.config.ConfigurationException;
import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.ConfigurationProvider;
import com.opensymphony.xwork.config.ConfigurationUtil;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.InterceptorConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import com.opensymphony.xwork.config.entities.ResultConfig;
import com.opensymphony.xwork.config.providers.InterceptorBuilder;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpaceAdminTabActionDescriptorFactory
{
    private static final String NAMESPACE_PREFIX = "/plugins/ac/";
    private static final String DEFAULT_INTERCEPTOR_STACK = "validatingStack";
    private static final String VELOCITY_TEMPLATE = "/velocity/space-tab-page.vm";

    private final EventPublisher eventPublisher;

    @Autowired
    public SpaceAdminTabActionDescriptorFactory(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    public ModuleDescriptor create(Plugin plugin, SpaceAdminTabContext context)
    {
        return new SpaceTabActionDescriptor(plugin, context);
    }

    private class SpaceTabActionDescriptor extends AbstractModuleDescriptor implements ConfigurationProvider
    {
        private final SpaceAdminTabContext context;

        public SpaceTabActionDescriptor(Plugin plugin, SpaceAdminTabContext context)
        {
            super(ModuleFactory.LEGACY_MODULE_FACTORY);
            this.context = context;

            Element element = new DOMElement("module")
                .addAttribute("key", "action-" + context.getWebItemKey());

            super.init(plugin, element);
        }

        @Override
        public void enabled()
        {
            super.enabled();

            ConfigurationManager.getConfigurationProviders();
            ConfigurationManager.addConfigurationProvider(this);

            eventPublisher.publish(new XWorkStateChangeEvent(this));
        }

        @Override
        public void disabled()
        {
            List configurationProviders = ConfigurationManager.getConfigurationProviders();
            synchronized (configurationProviders)
            {
                configurationProviders.remove(this);
            }

            eventPublisher.publish(new XWorkStateChangeEvent(this));

            super.disabled();
        }

        @Override
        public Object getModule()
        {
            return null;
        }

        @Override
        public void init(Configuration configuration) throws ConfigurationException
        {
            String key = context.getWebItemKey();

            List parentStack = ConfigurationUtil.buildParentsFromString(configuration, "default");
            String namespace = NAMESPACE_PREFIX + plugin.getKey();
            PackageConfig packageConfig = new PackageConfig(key, namespace, false, null, parentStack);

            InterceptorConfig interceptorConfig = new InterceptorConfig("space-tab-context",
                SpaceAdminTabContextInterceptor.class.getName(), Collections.EMPTY_MAP);
            ObjectFactory.getObjectFactory().buildInterceptor(interceptorConfig, Collections.EMPTY_MAP);
            packageConfig.addInterceptorConfig(interceptorConfig);

            Map<String, ResultConfig> results = Maps.newHashMap();
            Map resultParameters = ImmutableMap.of("location", VELOCITY_TEMPLATE);
            ResultConfig resultConfig = new ResultConfig("success", VelocityResult.class, resultParameters);
            results.put(resultConfig.getName(), resultConfig);

            ActionConfig actionConfig = new PluginAwareActionConfig(null, SpaceAdminIFrameAction.class.getName(),
                ImmutableMap.of("context", context), results, Lists.newArrayList(), plugin);
            actionConfig.addInterceptors(InterceptorBuilder.constructInterceptorReference(packageConfig,
                interceptorConfig.getName(), Collections.EMPTY_MAP));
            actionConfig.addInterceptors(InterceptorBuilder.constructInterceptorReference(packageConfig,
                DEFAULT_INTERCEPTOR_STACK, Collections.EMPTY_MAP));
            packageConfig.addActionConfig(key, actionConfig);

            configuration.addPackageConfig(key, packageConfig);
        }

        @Override
        public boolean needsReload()
        {
            return true;
        }
    }
}
