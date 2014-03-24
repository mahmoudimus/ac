package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.connect.plugin.module.DefaultWebItemContext;
import com.atlassian.plugin.connect.spi.module.UserIsAdminCondition;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.util.concurrent.NotNull;

import org.dom4j.Element;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates an admin page with a servlet containing an iframe and a web item
 */
public final class AdminPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final RemotePageDescriptorCreator remotePageDescriptorCreator;
    private final ProductAccessor productAccessor;
    private final UserIsAdminCondition userIsAdminCondition;
    private Element descriptor;
    private LegacyXmlDynamicDescriptorRegistration.Registration registration;

    public AdminPageModuleDescriptor(
            ModuleFactory moduleFactory,
            LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration,
            ProductAccessor productAccessor,
            RemotePageDescriptorCreator remotePageDescriptorCreator,
            UserIsAdminCondition userIsAdminCondition)
    {
        super(moduleFactory);
        this.productAccessor = productAccessor;
        this.userIsAdminCondition = userIsAdminCondition;
        this.remotePageDescriptorCreator = checkNotNull(remotePageDescriptorCreator);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
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
        final String sectionKey = getOptionalAttribute(descriptor, "section", productAccessor.getPreferredAdminSectionKey());
        final Integer weight = Integer.valueOf(getOptionalAttribute(descriptor, "weight", productAccessor.getPreferredAdminWeight()));

        final RemotePageDescriptorCreator.Builder remotePageDescriptorBuilder = remotePageDescriptorCreator.newBuilder()
            .setWebItemContext(new DefaultWebItemContext(sectionKey, weight, productAccessor.getLinkContextParams()))
            .setDecorator("atl.admin")
            .setCondition(userIsAdminCondition);

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
