package com.atlassian.plugin.connect.jira.search;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@JiraComponent
public class SearchRequestViewModuleProvider extends AbstractJiraConnectModuleProvider<SearchRequestViewModuleBean> {

    private static final SearchRequestViewModuleMeta META = new SearchRequestViewModuleMeta();

    private final SearchRequestViewModuleDescriptorFactory searchRequestViewModuleDescriptorFactory;
    private final ConditionLoadingValidator conditionLoadingValidator;

    @Autowired
    public SearchRequestViewModuleProvider(PluginRetrievalService pluginRetrievalService,
                                           ConnectJsonSchemaValidator schemaValidator,
                                           SearchRequestViewModuleDescriptorFactory searchRequestViewModuleDescriptorFactory,
                                           ConditionLoadingValidator conditionLoadingValidator) {
        super(pluginRetrievalService, schemaValidator);
        this.searchRequestViewModuleDescriptorFactory = searchRequestViewModuleDescriptorFactory;
        this.conditionLoadingValidator = conditionLoadingValidator;
    }

    @Override
    public ConnectModuleMeta<SearchRequestViewModuleBean> getMeta() {
        return META;
    }

    @Override
    public List<SearchRequestViewModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException {
        List<SearchRequestViewModuleBean> searchRequestViews = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        conditionLoadingValidator.validate(pluginRetrievalService.getPlugin(), descriptor, getMeta(), searchRequestViews);
        return searchRequestViews;
    }

    @Override
    public List<ModuleDescriptor<?>> createPluginModuleDescriptors(List<SearchRequestViewModuleBean> modules, ConnectAddonBean addon) {
        List<ModuleDescriptor<?>> moduleDescriptors = new ArrayList<>();

        for (SearchRequestViewModuleBean bean : modules) {
            ModuleDescriptor descriptor = searchRequestViewModuleDescriptorFactory.createModuleDescriptor(
                    bean, addon, pluginRetrievalService.getPlugin());
            moduleDescriptors.add(descriptor);
        }

        return moduleDescriptors;
    }
}
