package com.atlassian.plugin.remotable.plugin.module.page;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

/**
 * Generates a profile page with a servlet containing an iframe and a web item
 */
public class ProfilePageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final RemotePageDescriptorCreator.Builder remotePageDescriptorBuilder;
    private Element descriptor;
    private DynamicDescriptorRegistration.Registration registration;

    public ProfilePageModuleDescriptor(DynamicDescriptorRegistration dynamicDescriptorRegistration,
            RemotePageDescriptorCreator remotePageDescriptorCreator)
    {
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.remotePageDescriptorBuilder = remotePageDescriptorCreator.newBuilder()
                .setDecorator("atl.userprofile")
                .setTemplateSuffix("");

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
        this.registration = dynamicDescriptorRegistration.registerDescriptors(getPlugin(),
                remotePageDescriptorBuilder.build(getPlugin(), descriptor));
    }

    @Override
    public void disabled()
    {
        super.disabled();
        if (registration != null)
        {
            registration.unregister();
        }
    }
}
