package com.atlassian.plugin.connect.confluence.web;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ProfilePageModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.spi.ProductAccessor;
import com.atlassian.plugin.connect.spi.lifecycle.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.api.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.List;

@ConfluenceComponent
public class ProfilePageModuleProvider extends AbstractConnectPageModuleProvider {

    private static final ProfilePageModuleMeta META = new ProfilePageModuleMeta();

    private final PluginRetrievalService pluginRetrievalService;
    private final ConnectJsonSchemaValidator schemaValidator;
    private final ProductAccessor productAccessor;

    @Autowired
    public ProfilePageModuleProvider(PluginRetrievalService pluginRetrievalService,
                                     IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                     IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                     WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                     ConditionClassAccessor conditionClassAccessor,
                                     ConnectJsonSchemaValidator schemaValidator,
                                     ConditionLoadingValidator conditionLoadingValidator,
                                     ProductAccessor productAccessor) {
        super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, conditionClassAccessor, conditionLoadingValidator);
        this.pluginRetrievalService = pluginRetrievalService;
        this.schemaValidator = schemaValidator;
        this.productAccessor = productAccessor;
    }

    @Override
    public ConnectModuleMeta<ConnectPageModuleBean> getMeta() {
        return META;
    }

    @Override
    public List<ConnectPageModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry,
                                                                         ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException {
        URL schemaUrl = pluginRetrievalService.getPlugin().getResource("/schema/confluence-schema.json");
        assertDescriptorValidatesAgainstSchema(jsonModuleListEntry, descriptor, schemaUrl, schemaValidator);
        return super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
    }

    @Override
    protected int getDefaultWeight() {
        return productAccessor.getPreferredProfileWeight();
    }

    @Override
    protected String getDefaultSection() {
        return productAccessor.getPreferredProfileSectionKey();
    }

    @Override
    protected String getDecorator() {
        return "atl.userprofile";
    }
}
