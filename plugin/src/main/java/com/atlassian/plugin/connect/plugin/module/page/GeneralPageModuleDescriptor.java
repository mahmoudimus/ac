package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates a general page with a servlet containing an iframe and a web item
 */
public final class GeneralPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final RemotePageDescriptorCreator.Builder remotePageDescriptorBuilder;
    private Element descriptor;
    private LegacyXmlDynamicDescriptorRegistration.Registration registration;
    private static final Logger log = LoggerFactory.getLogger(GeneralPageModuleDescriptor.class);

    public GeneralPageModuleDescriptor(
            ModuleFactory moduleFactory,
            LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration,
            RemotePageDescriptorCreator remotePageDescriptorCreator)
    {
        super(moduleFactory);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.remotePageDescriptorBuilder = checkNotNull(remotePageDescriptorCreator).newBuilder()
                .setDecorator("atl.general").addIframeContextParam("general", "1");
    }

    @Override
    public Void getModule()
    {
        return null;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.descriptor = element;
    }

    @Override
    public void enabled()
    {
        super.enabled();
        log.debug("Enabling general page {} instance {}", getKey(), System.identityHashCode(this));
        this.registration = dynamicDescriptorRegistration.registerDescriptors(getPlugin(),
                remotePageDescriptorBuilder.build(getPlugin(), descriptor));
    }

    @Override
    public void disabled()
    {
        log.debug("Disabling general page {} instance {}" , getKey(), System.identityHashCode(this));
        super.disabled();
        if (registration != null)
        {
            log.debug("Unregistering dynamic descriptors for {} instance {}", getKey(), System.identityHashCode(this));
            registration.unregister();
        }
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
