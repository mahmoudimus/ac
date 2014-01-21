package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean;
import com.atlassian.plugin.connect.plugin.module.XWorkPackageCreator;
import com.atlassian.plugin.connect.plugin.module.confluence.XWorkActionDescriptor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Builds XWork package configurations and bundles them in XWorkActionDescriptors.
 */
@ConfluenceComponent
public class XWorkActionDescriptorFactory
{
    private final EventPublisher eventPublisher;

    @Autowired
    public XWorkActionDescriptorFactory(final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    public XWorkActionDescriptor create(Plugin plugin, XWorkActionModuleBean actionModuleBean)
    {
        String moduleKey = "action-" + actionModuleBean.getKey();
        XWorkPackageCreator packageCreator = new XWorkPackageCreator(plugin, actionModuleBean);

        return new XWorkActionDescriptor(eventPublisher, plugin, moduleKey, packageCreator);
    }
}
