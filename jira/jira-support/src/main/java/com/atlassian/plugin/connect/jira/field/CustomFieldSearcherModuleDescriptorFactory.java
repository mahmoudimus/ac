package com.atlassian.plugin.connect.jira.field;

import com.atlassian.jira.plugin.customfield.CustomFieldDefaultVelocityParams;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptorImpl;
import com.atlassian.jira.render.Encoder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.field.type.ConnectFieldTypeBlueprintResolver;
import com.atlassian.plugin.connect.jira.field.type.SearcherDefinition;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldModuleBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class CustomFieldSearcherModuleDescriptorFactory implements ConnectModuleDescriptorFactory<ConnectFieldModuleBean, CustomFieldSearcherModuleDescriptor> {
    private final JiraAuthenticationContext authenticationContext;
    private final ModuleFactory moduleFactory;
    private final Encoder encoder;
    private final ConnectFieldTypeBlueprintResolver connectFieldTypeBlueprintResolver;

    @Autowired
    public CustomFieldSearcherModuleDescriptorFactory(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory, final Encoder encoder, final ConnectFieldTypeBlueprintResolver connectFieldTypeBlueprintResolver) {
        this.authenticationContext = authenticationContext;
        this.moduleFactory = moduleFactory;
        this.encoder = encoder;
        this.connectFieldTypeBlueprintResolver = connectFieldTypeBlueprintResolver;
    }

    @Override
    public CustomFieldSearcherModuleDescriptor createModuleDescriptor(final ConnectFieldModuleBean bean, final ConnectAddonBean addon, final Plugin plugin) {
        CustomFieldSearcherModuleDescriptor descriptor = new CustomFieldSearcherModuleDescriptorImpl(authenticationContext, moduleFactory, new CustomFieldDefaultVelocityParams(encoder));

        Element element = new DOMElement("customfield-searcher");

        String i18nKeyOrName = Strings.isNullOrEmpty(bean.getName().getI18n()) ? bean.getDisplayName() : bean.getName().getI18n();

        element.addAttribute("key", searcherKeyFromCustomFieldTypeKey(bean.getKey(addon)));
        element.addAttribute("i18n-name-key", i18nKeyOrName);

        SearcherDefinition searcher = connectFieldTypeBlueprintResolver.getBlueprint(bean.getType()).getSearcherDefinition();

        element.addAttribute("class", searcher.getSearcherClassFullyQualifiedName());
        element.add(velocityResourceElement("view", searcher.getViewTemplate()));
        element.add(velocityResourceElement("search", searcher.getSearchTemplate()));

        element.add(validCustomFieldType(plugin.getKey(), bean.getKey(addon)));

        descriptor.init(plugin, element);
        return descriptor;
    }

    private Element velocityResourceElement(String name, String location) {
        DOMElement resource = new DOMElement("resource");
        resource.addAttribute("type", "velocity");
        resource.addAttribute("name", name);
        resource.addAttribute("location", location);
        return resource;
    }

    private Element validCustomFieldType(String addOnKey, String customFieldKey) {
        DOMElement resource = new DOMElement("valid-customfield-type");
        resource.addAttribute("package", addOnKey);
        resource.addAttribute("key", customFieldKey);
        return resource;
    }

    public static String searcherKeyFromCustomFieldTypeKey(String completeKey) {
        return completeKey + "_searcher";
    }
}
