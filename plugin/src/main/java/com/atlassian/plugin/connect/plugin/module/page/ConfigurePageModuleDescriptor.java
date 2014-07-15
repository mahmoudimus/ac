package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.DefaultWebItemContext;
import com.atlassian.plugin.connect.spi.condition.UserIsAdminCondition;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import static com.atlassian.plugin.connect.plugin.module.page.RemotePageDescriptorCreator.createLocalUrl;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates an admin configuration page with a servlet containing an iframe
 */
public final class ConfigurePageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final RemotePageDescriptorCreator.Builder remotePageDescriptorBuilder;
    private Element descriptor;
    private LegacyXmlDynamicDescriptorRegistration.Registration registration;

    public ConfigurePageModuleDescriptor(
            ModuleFactory moduleFactory,
            LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration,
            ProductAccessor productAccessor,
            RemotePageDescriptorCreator remotePageDescriptorCreator,
            UserIsAdminCondition userIsAdminCondition)
    {
        super(moduleFactory);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.remotePageDescriptorBuilder = checkNotNull(remotePageDescriptorCreator).newBuilder()
                .setWebItemContext(new DefaultWebItemContext(
                        "no-section",
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

        checkNotNull(plugin.getPluginInformation().getParameters().get("configure.url"),
                "You need to set this configuration: <plugin-info><param name=\"configure.url\">" +
                        "/plugins/servlet" + createLocalUrl(getPluginKey(), getKey()) + "</param></plugin-info>");
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

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
