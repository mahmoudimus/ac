package com.atlassian.plugin.connect.jira.customfield;

import com.atlassian.jira.plugin.customfield.CustomFieldDefaultVelocityParams;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptorImpl;
import com.atlassian.jira.render.Encoder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.CustomFieldBaseType.CustomFieldSearcherDefinition;
import com.atlassian.plugin.connect.modules.beans.CustomFieldTypeModuleBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class CustomFieldSearcherDescriptorFactory implements ConnectModuleDescriptorFactory<CustomFieldTypeModuleBean, CustomFieldSearcherModuleDescriptor>
{
    private final JiraAuthenticationContext authenticationContext;
    private final ModuleFactory moduleFactory;
    private final Encoder encoder;

    @Autowired
    public CustomFieldSearcherDescriptorFactory(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory, final Encoder encoder)
    {
        this.authenticationContext = authenticationContext;
        this.moduleFactory = moduleFactory;
        this.encoder = encoder;
    }

    @Override
    public CustomFieldSearcherModuleDescriptor createModuleDescriptor(final CustomFieldTypeModuleBean bean, final ConnectAddonBean addon, final Plugin plugin)
    {
        CustomFieldSearcherModuleDescriptor descriptor = new CustomFieldSearcherModuleDescriptorImpl(authenticationContext, moduleFactory, new CustomFieldDefaultVelocityParams(encoder));

        Element element = new DOMElement("customfield-searcher");

        String i18nKeyOrName = Strings.isNullOrEmpty(bean.getName().getI18n()) ? bean.getDisplayName() : bean.getName().getI18n();

        element.addAttribute("key", bean.getKey(addon)+"_searcher");
        element.addAttribute("i18n-name-key", i18nKeyOrName);

        CustomFieldSearcherDefinition type = bean.getBaseTypeConfiguration().getBaseType().getSearcherBase();

        element.addAttribute("class", type.getClassFQN());
        element.add(velocityResourceElement("view", type.getViewTemplate()));
        element.add(velocityResourceElement("search", type.getSearchTemplate()));

        element.add(validCustomFieldType(plugin.getKey(), bean.getKey(addon)));

        descriptor.init(plugin, element);
        return descriptor;
    }

    private Element velocityResourceElement(String name, String location)
    {
        DOMElement resource = new DOMElement("resource");
        resource.addAttribute("type", "velocity");
        resource.addAttribute("name", name);
        resource.addAttribute("location", location);
        return resource;
    }

    private Element validCustomFieldType(String addOnKey, String customFieldKey)
    {
        DOMElement resource = new DOMElement("valid-customfield-type");
        resource.addAttribute("package", addOnKey);
        resource.addAttribute("key", customFieldKey);
        return resource;
    }
}
