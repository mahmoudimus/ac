package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ProfilePageModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.capabilities.provider.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.spi.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.module.ConnectModuleValidationException;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.List;

@ConfluenceComponent
public class ProfilePageModuleProvider extends AbstractConnectPageModuleProvider
{

    private static final ProfilePageModuleMeta META = new ProfilePageModuleMeta();

    private final PluginRetrievalService pluginRetrievalService;
    private final ConnectJsonSchemaValidator schemaValidator;
    private final ProductAccessor productAccessor;

    @Autowired
    public ProfilePageModuleProvider(PluginRetrievalService pluginRetrievalService,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            PageConditionsFactory pageConditionsFactory,
            ConnectJsonSchemaValidator schemaValidator,
            ProductAccessor productAccessor)
    {
        super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, pageConditionsFactory);
        this.pluginRetrievalService = pluginRetrievalService;
        this.schemaValidator = schemaValidator;
        this.productAccessor = productAccessor;
    }

    @Override
    public ConnectModuleMeta<ConnectPageModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ConnectPageModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry,
            ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        URL schemaUrl = pluginRetrievalService.getPlugin().getResource("/schema/confluence-schema.json");
        assertDescriptorValidatesAgainstSchema(jsonModuleListEntry, schemaUrl, schemaValidator);
        return super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
    }

    @Override
    protected int getDefaultWeight()
    {
        return productAccessor.getPreferredProfileWeight();
    }

    @Override
    protected String getDefaultSection()
    {
        return productAccessor.getPreferredProfileSectionKey();
    }

    @Override
    protected String getDecorator()
    {
        return "atl.userprofile";
    }
}
