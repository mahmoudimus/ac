package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import java.util.Collections;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.provider.GeneralPageModuleProvider;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsActionDescriptor;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.plugin.module.page.SpaceToolsTabContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpaceToolsActionDescriptorFactory
{
    public static final String NAMESPACE_PREFIX = "/plugins/ac/";

    private final EventPublisher eventPublisher;

    @Autowired
    public SpaceToolsActionDescriptorFactory(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    public SpaceToolsActionDescriptor create(Plugin plugin, String spaceToolsWebItemKey, String spaceAdminWebItemKey, String title, String remoteUrl)
    {
        PageInfo pageInfo = new PageInfo(GeneralPageModuleProvider.GENERAL_PAGE_DECORATOR, "", title, null, Collections.EMPTY_MAP);
        SpaceToolsTabContext spaceTabContext = new SpaceToolsTabContext(plugin, remoteUrl, spaceToolsWebItemKey, spaceAdminWebItemKey, pageInfo);
        String moduleKey = "action-" + spaceToolsWebItemKey;
        String namespace = NAMESPACE_PREFIX + plugin.getKey();
        return new SpaceToolsActionDescriptor(eventPublisher, plugin, moduleKey, spaceTabContext, namespace, spaceToolsWebItemKey);
    }
}
