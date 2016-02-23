package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.plugin.lifecycle.ConnectModuleProviderModuleDescriptor;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleProvider;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Component
public class ConnectAddonBeanModuleValidatorService {

    private final PluginAccessor pluginAccessor;

    @Autowired
    public ConnectAddonBeanModuleValidatorService(PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
    }

    public Map<String, List<ModuleBean>> validateModules(ConnectAddonBean addon, Consumer<Exception> exceptionHandler)
            throws InvalidDescriptorException {
        Map<String, List<ModuleBean>> validModuleLists = addon.getModules().getValidModuleLists(exceptionHandler);

        Map<String, ConnectModuleProvider> moduleProviders = getModuleProviders();
        for (Map.Entry<String, List<ModuleBean>> validModuleList : validModuleLists.entrySet()) {
            ConnectModuleProvider moduleProvider = moduleProviders.get(validModuleList.getKey());
            try {
                moduleProvider.validateModuleDependencies(addon);
            } catch (Exception e) {
                exceptionHandler.accept(e);
            }
        }
        return validModuleLists;
    }

    private Map<String, ConnectModuleProvider> getModuleProviders() {
        return pluginAccessor.getModules(new ModuleDescriptorOfClassPredicate<>(ConnectModuleProviderModuleDescriptor.class))
                .stream().collect(Collectors.toMap(provider -> provider.getMeta().getDescriptorKey(), identity()));
    }
}
