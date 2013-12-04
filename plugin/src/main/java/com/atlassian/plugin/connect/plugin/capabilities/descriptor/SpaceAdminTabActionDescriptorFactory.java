package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsActionDescriptor;
import com.atlassian.plugin.connect.plugin.module.page.SpaceAdminTabContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpaceAdminTabActionDescriptorFactory
{
    private final EventPublisher eventPublisher;

    @Autowired
    public SpaceAdminTabActionDescriptorFactory(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    public ModuleDescriptor create(Plugin plugin, SpaceAdminTabContext context)
    {
        return new SpaceToolsActionDescriptor(eventPublisher, plugin, context);
    }

}
