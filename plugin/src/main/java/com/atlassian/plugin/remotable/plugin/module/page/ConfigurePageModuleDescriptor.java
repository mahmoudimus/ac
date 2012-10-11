package com.atlassian.plugin.remotable.plugin.module.page;

import com.atlassian.plugin.remotable.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.remotable.spi.module.UserIsAdminCondition;
import com.atlassian.plugin.remotable.plugin.module.DefaultWebItemContext;
import com.atlassian.plugin.remotable.plugin.product.ProductAccessor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import static com.atlassian.plugin.remotable.plugin.module.page.RemotePageDescriptorCreator.createLocalUrl;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates an admin configuration page with a servlet containing an iframe
 */
public class ConfigurePageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final RemotePageDescriptorCreator.Builder remotePageDescriptorBuilder;
    private Element descriptor;

    public ConfigurePageModuleDescriptor(
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            ProductAccessor productAccessor,
            RemotePageDescriptorCreator remotePageDescriptorCreator,
            UserIsAdminCondition userIsAdminCondition)
    {
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.remotePageDescriptorBuilder = remotePageDescriptorCreator.newBuilder()
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
        dynamicDescriptorRegistration.registerDescriptors(getPlugin(),
                remotePageDescriptorBuilder.build(getPlugin(), descriptor));
    }
}
