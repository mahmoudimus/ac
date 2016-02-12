package com.atlassian.plugin.connect.jira.field;

import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.plugin.customfield.CustomFieldDefaultVelocityParams;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.render.Encoder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldModuleBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class ConnectFieldModuleDescriptorFactory implements ConnectModuleDescriptorFactory<ConnectFieldModuleBean, ConnectFieldModuleDescriptor>
{

    private final JiraAuthenticationContext authenticationContext;
    private final RendererManager rendererManager;
    private final ModuleFactory moduleFactory;
    private final Encoder encoder;

    private final CustomFieldManager customFieldManager;
    private final ProjectManager projectManager;
    private final ManagedConfigurationItemService managedConfigurationItemService;

    private final ConnectFieldMapper connectFieldMapper;

    @Autowired
    public ConnectFieldModuleDescriptorFactory(final JiraAuthenticationContext authenticationContext, final RendererManager rendererManager, final ModuleFactory moduleFactory, final Encoder encoder, final CustomFieldManager customFieldManager, final ProjectManager projectManager, final ManagedConfigurationItemService managedConfigurationItemService, final ConnectFieldMapper connectFieldMapper)
    {
        this.authenticationContext = authenticationContext;
        this.rendererManager = rendererManager;
        this.moduleFactory = moduleFactory;
        this.encoder = encoder;
        this.customFieldManager = customFieldManager;
        this.projectManager = projectManager;
        this.managedConfigurationItemService = managedConfigurationItemService;
        this.connectFieldMapper = connectFieldMapper;
    }

    @Override
    public ConnectFieldModuleDescriptor createModuleDescriptor(final ConnectFieldModuleBean bean, final ConnectAddonBean addon, final Plugin plugin)
    {
        ConnectFieldModuleDescriptor descriptor = new ConnectFieldModuleDescriptor(authenticationContext, rendererManager, moduleFactory, new CustomFieldDefaultVelocityParams(encoder), customFieldManager, projectManager, managedConfigurationItemService);

        Element element = new DOMElement("customfield-type");

        String i18nKeyOrName = Strings.isNullOrEmpty(bean.getName().getI18n()) ? bean.getDisplayName() : bean.getName().getI18n();

        element.addAttribute("key", bean.getKey(addon));
        element.addAttribute("i18n-name-key", i18nKeyOrName);
        element.addAttribute("managed-access-level", "locked");

        DOMElement description = new DOMElement("description");
        description.setText(bean.getDescription().getValue());

        element.add(description);

        ConnectFieldMapper.BaseTypeDefinition type = connectFieldMapper.getMapping(bean.getType()).getType();

        element.addAttribute("class", type.getBaseCFTypeClassFullyQualifiedName());
        element.add(velocityResourceElement("view", type.getViewTemplate()));
        element.add(velocityResourceElement("edit", type.getEditTemplate()));
        element.add(velocityResourceElement("xml", type.getXmlTemplate()));

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
