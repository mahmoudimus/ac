package com.atlassian.plugin.connect.confluence.web.spacetools;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean;
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

    public XWorkActionDescriptor create(ConnectAddonBean addon, Plugin plugin, XWorkActionModuleBean actionModuleBean)
    {
        String moduleKey = "action-" + actionModuleBean.getRawKey();
        XWorkPackageCreator packageCreator = new XWorkPackageCreator(addon, plugin, actionModuleBean);

        return new XWorkActionDescriptor(eventPublisher, plugin, moduleKey, packageCreator);
    }
}
