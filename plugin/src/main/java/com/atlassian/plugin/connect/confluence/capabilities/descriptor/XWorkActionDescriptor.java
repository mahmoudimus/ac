package com.atlassian.plugin.connect.confluence.capabilities.descriptor;

import com.atlassian.confluence.event.events.plugin.XWorkStateChangeEvent;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.confluence.capabilities.provider.XWorkPackageCreator;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.opensymphony.xwork.config.Configuration;
import com.opensymphony.xwork.config.ConfigurationException;
import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.ConfigurationProvider;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;

import java.util.List;

public class XWorkActionDescriptor extends AbstractModuleDescriptor implements ConfigurationProvider
{
    private static final ModuleFactory NOOP_MODULE_FACTORY = new NoOpModuleFactory();

    private final EventPublisher eventPublisher;
    private final XWorkPackageCreator xWorkPackageCreator;

    public XWorkActionDescriptor(final EventPublisher eventPublisher, final Plugin plugin, final String moduleKey, final XWorkPackageCreator xWorkPackageCreator)
    {
        super(NOOP_MODULE_FACTORY);
        this.eventPublisher = eventPublisher;
        this.xWorkPackageCreator = xWorkPackageCreator;

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
        xWorkPackageCreator.createAndRegister(configuration);
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

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
