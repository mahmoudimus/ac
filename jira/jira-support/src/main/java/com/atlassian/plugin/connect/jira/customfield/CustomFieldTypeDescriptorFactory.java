package com.atlassian.plugin.connect.jira.customfield;

import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.plugin.customfield.CustomFieldDefaultVelocityParams;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptorImpl;
import com.atlassian.jira.render.Encoder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.CustomFieldArchetype;
import com.atlassian.plugin.connect.modules.beans.CustomFieldArchetype.CustomFieldBaseType;
import com.atlassian.plugin.connect.modules.beans.CustomFieldTypeModuleBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class CustomFieldTypeDescriptorFactory implements ConnectModuleDescriptorFactory<CustomFieldTypeModuleBean, CustomFieldTypeModuleDescriptor>
{

    private final JiraAuthenticationContext authenticationContext;
    private final RendererManager rendererManager;
    private final ModuleFactory moduleFactory;
    private final Encoder encoder;

    @Autowired
    public CustomFieldTypeDescriptorFactory(final JiraAuthenticationContext authenticationContext, final RendererManager rendererManager, final ModuleFactory moduleFactory, final Encoder encoder)
    {
        this.authenticationContext = authenticationContext;
        this.rendererManager = rendererManager;
        this.moduleFactory = moduleFactory;
        this.encoder = encoder;
    }

    @Override
    public CustomFieldTypeModuleDescriptor createModuleDescriptor(final CustomFieldTypeModuleBean bean, final ConnectAddonBean addon, final Plugin plugin)
    {
        CustomFieldTypeModuleDescriptorImpl descriptor = new CustomFieldTypeModuleDescriptorImpl(authenticationContext, rendererManager, moduleFactory, new CustomFieldDefaultVelocityParams(encoder));

        Element element = new DOMElement("customfield-type");

        String i18nKeyOrName = Strings.isNullOrEmpty(bean.getName().getI18n()) ? bean.getDisplayName() : bean.getName().getI18n();

        element.addAttribute("key", bean.getKey(addon));
        element.addAttribute("i18n-name-key", i18nKeyOrName);

        DOMElement description = new DOMElement("description");
        description.setText(bean.getDescription().getValue());

        element.add(description);

        CustomFieldBaseType type = bean.getArchetypeConfiguration().getArchetype().getType();

        element.addAttribute("class", type.getClassFQN());
        element.add(velocityResourceElement("view", type.getViewTemplate()));
        element.add(velocityResourceElement("edit", type.getEditTemplate()));

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
}
