package com.atlassian.plugin.connect.reference;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.spi.lifecycle.AbstractConnectModuleProvider;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.util.validation.ValidationPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ReferenceModuleProvider extends AbstractConnectModuleProvider<ReferenceModuleBean>
{

    private static final ReferenceModuleMeta META = new ReferenceModuleMeta();

    private PluginRetrievalService pluginRetrievalService;

    @Autowired
    public ReferenceModuleProvider(@ComponentImport PluginRetrievalService pluginRetrievalService)
    {
        this.pluginRetrievalService = pluginRetrievalService;
    }

    @Override
    public ConnectModuleMeta<ReferenceModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ReferenceModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        List<ReferenceModuleBean> referenceModules = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        assertReferenceFieldValid(descriptor, referenceModules);
        return referenceModules;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ReferenceModuleBean> referenceModules, ConnectAddonBean addon)
    {
        return referenceModules.stream().map((Function<ReferenceModuleBean, ModuleDescriptor>) referenceModule -> {
            String moduleKey = referenceModule.getKey(addon);
            FakeModuleDescriptor moduleDescriptor = new FakeModuleDescriptor(moduleKey);
            moduleDescriptor.setPlugin(pluginRetrievalService.getPlugin());
            return moduleDescriptor;
        }).collect(Collectors.toList());
    }

    private void assertReferenceFieldValid(ShallowConnectAddonBean descriptor, List<ReferenceModuleBean> referenceModules)
            throws ConnectModuleValidationException
    {
        for (ReferenceModuleBean referenceModule : referenceModules)
        {
            if (referenceModule.getReferenceField() > Integer.MAX_VALUE)
            {
                String message = "Installation failed. The add-on includes a reference module with an impossibly large reference field value.";
                String i18nKey = "connect.install.error.reference.field.invalid";
                throw new ConnectModuleValidationException(descriptor, getMeta(), message, i18nKey);
            }
        }
    }

    /**
     * The lifecycle of add-ons relies on there being at least one plugin module descriptor per add-on. This fake
     * descriptor is used to fulfill that requirement.
     */
    private static class FakeModuleDescriptor extends AbstractModuleDescriptor<Void>
    {

        public FakeModuleDescriptor(String key)
        {
            super(ModuleFactory.LEGACY_MODULE_FACTORY);
            this.key = key;
        }

        @Override
        public Void getModule()
        {
            return null;
        }

        /**
         * Overridden to avoid the requirement of the descriptor having an XML element with a <pre>key</pre> attribute.
         *
         * @param pattern The validation pattern
         */
        @Override
        protected void provideValidationRules(ValidationPattern pattern)
        {}
    }
}
