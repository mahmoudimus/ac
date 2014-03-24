package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
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

    public XWorkActionDescriptor create(ConnectAddonBean addon, Plugin theConnectPlugin, XWorkActionModuleBean actionModuleBean)
    {
        String moduleKey = "action-" + actionModuleBean.getRawKey();
        XWorkPackageCreator packageCreator = new XWorkPackageCreator(addon, theConnectPlugin, actionModuleBean);

        return new XWorkActionDescriptor(eventPublisher, theConnectPlugin, moduleKey, packageCreator);
    }
}
