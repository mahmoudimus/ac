package com.atlassian.plugin.remotable.plugin.module.page;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.remotable.plugin.module.DefaultWebItemContext;
import com.atlassian.plugin.remotable.plugin.util.node.Dom4jNode;
import com.atlassian.plugin.remotable.spi.module.UserIsAdminCondition;
import com.atlassian.plugin.remotable.spi.product.ProductAccessor;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates an admin page with a servlet containing an iframe and a web item
 */
public final class AdminPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final RemotePageDescriptorCreator.Builder remotePageDescriptorBuilder;
    private Element descriptor;
    private DynamicDescriptorRegistration.Registration registration;

    public AdminPageModuleDescriptor(
            ModuleFactory moduleFactory,
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            ProductAccessor productAccessor,
            RemotePageDescriptorCreator remotePageDescriptorCreator,
            UserIsAdminCondition userIsAdminCondition)
    {
        super(moduleFactory);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.remotePageDescriptorBuilder = checkNotNull(remotePageDescriptorCreator).newBuilder()
                .setWebItemContext(new DefaultWebItemContext(
                        productAccessor.getPreferredAdminSectionKey(),
                        productAccessor.getPreferredAdminWeight(),
                        productAccessor.getLinkContextParams()
                ))
                .setDecorator("atl.admin")
                .setCondition(userIsAdminCondition);

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
                remotePageDescriptorBuilder.build(getPlugin(), new Dom4jNode(descriptor)));
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
