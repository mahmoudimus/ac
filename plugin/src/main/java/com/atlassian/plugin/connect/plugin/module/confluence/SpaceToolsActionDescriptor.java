package com.atlassian.plugin.connect.plugin.module.confluence;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.confluence.event.events.plugin.XWorkStateChangeEvent;
import com.atlassian.confluence.plugin.descriptor.PluginAwareActionConfig;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
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

/**
 * This is a special-case descriptor that models an XWork action to display a Space Tools page. There is no clean
 * page decorators that can be used to render the space tools pages - they have a hardcoded expectation that they're
 * rendering from inside an xwork action context. If at some point this situation changes, it may be feasible to
 * model Space Tools pages simply using PageToWebItemAndServletConverter
 */
public class SpaceToolsActionDescriptor extends AbstractModuleDescriptor implements ConfigurationProvider
{
    private static final ModuleFactory NOOP_MODULE_FACTORY = new NoOpModuleFactory();
    private static final String DEFAULT_INTERCEPTOR_STACK = "validatingStack";
    private static final String VELOCITY_TEMPLATE = "/velocity/space-tab-page.vm";

    private final EventPublisher eventPublisher;
    private final SpaceAdminTabContext context;
    private final String namespace;

    public SpaceToolsActionDescriptor(EventPublisher eventPublisher, Plugin plugin, String moduleKey, SpaceAdminTabContext context, String namespace)
    {
        super(NOOP_MODULE_FACTORY);
        this.eventPublisher = eventPublisher;
        this.context = context;
        this.namespace = namespace;

        Element element = new DOMElement("module")
            .addAttribute("key", moduleKey);

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
        List parentStack = ConfigurationUtil.buildParentsFromString(configuration, "default");
        PackageConfig packageConfig = new PackageConfig(key, namespace, false, null, parentStack);

        InterceptorConfig interceptorConfig = new InterceptorConfig("space-tab-context",
            SpaceAdminTabContextInterceptor.class.getName(), Collections.EMPTY_MAP);
        ObjectFactory.getObjectFactory().buildInterceptor(interceptorConfig, Collections.EMPTY_MAP);
        packageConfig.addInterceptorConfig(interceptorConfig);

        Map<String, ResultConfig> results = Maps.newHashMap();
        Map resultParameters = ImmutableMap.of("location", VELOCITY_TEMPLATE);
        ResultConfig resultConfig = new ResultConfig("success", VelocityResult.class, resultParameters);
        results.put(resultConfig.getName(), resultConfig);

        Map<String, Object> actionParameters = ImmutableMap.<String, Object>of("context", context);
        ActionConfig actionConfig = new PluginAwareActionConfig(null, SpaceAdminIFrameAction.class.getName(),
            actionParameters, results, Lists.newArrayList(), plugin);
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

    private static class NoOpModuleFactory implements ModuleFactory
    {
        @Override
        public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
        {
            throw new IllegalStateException("The ModuleFactory for " + moduleDescriptor.getModuleClass().getName() + " is expected to never be called.");
        }
    }
}
