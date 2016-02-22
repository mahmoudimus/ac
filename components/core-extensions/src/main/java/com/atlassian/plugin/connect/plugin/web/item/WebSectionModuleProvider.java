package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleMeta;
import com.atlassian.plugin.connect.plugin.AbstractConnectCoreModuleProvider;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebSectionModuleProvider extends AbstractConnectCoreModuleProvider<WebSectionModuleBean> {

    private static final WebSectionModuleMeta META = new WebSectionModuleMeta();

    private final ConnectWebSectionModuleDescriptorFactory webSectionFactory;
    private final ConditionLoadingValidator conditionLoadingValidator;

    @Autowired
    public WebSectionModuleProvider(PluginRetrievalService pluginRetrievalService,
                                    ConnectJsonSchemaValidator schemaValidator,
                                    ConnectWebSectionModuleDescriptorFactory webSectionFactory,
                                    ConditionLoadingValidator conditionLoadingValidator) {
        super(pluginRetrievalService, schemaValidator);
        this.webSectionFactory = webSectionFactory;
        this.conditionLoadingValidator = conditionLoadingValidator;
    }

    @Override
    public ConnectModuleMeta<WebSectionModuleBean> getMeta() {
        return META;
    }

    @Override
    public List<WebSectionModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException {
        List<WebSectionModuleBean> webSections = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        conditionLoadingValidator.validate(pluginRetrievalService.getPlugin(), descriptor, getMeta(), webSections);
        return webSections;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<WebSectionModuleBean> modules, ConnectAddonBean addon) {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (WebSectionModuleBean webSection : modules) {
            descriptors.add(webSectionFactory.createModuleDescriptor(webSection, addon, pluginRetrievalService.getPlugin()));
        }
        return descriptors;
    }

}
