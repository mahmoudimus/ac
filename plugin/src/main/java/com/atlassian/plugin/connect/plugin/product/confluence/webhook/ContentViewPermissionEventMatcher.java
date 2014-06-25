package com.atlassian.plugin.connect.plugin.product.confluence.webhook;

import com.atlassian.confluence.event.events.content.ContentEvent;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.user.User;
import com.atlassian.webhooks.spi.provider.EventMatcher;
import com.atlassian.webhooks.spi.provider.PluginModuleListenerParameters;
import org.apache.commons.lang3.StringUtils;

/**
 * Performs checks to ensure the addon user can view the content in the given ContentEvent.
 * If not, the event is filtered out.
 */
public class ContentViewPermissionEventMatcher implements EventMatcher<ContentEvent>
{
    private final UserAccessor userAccessor;
    private final PermissionManager confluencePermissionManager;
    private final ConnectAddonRegistry addonRegistry;

    public ContentViewPermissionEventMatcher(UserAccessor userAccessor, PermissionManager confluencePermissionManager, ConnectAddonRegistry addonRegistry)
    {
        this.userAccessor = userAccessor;
        this.confluencePermissionManager = confluencePermissionManager;
        this.addonRegistry = addonRegistry;
    }

    @Override
    public boolean matches(ContentEvent contentEvent, Object listenerParameters)
    {
        if(listenerParameters == null || !(listenerParameters instanceof PluginModuleListenerParameters)) {
            return false;
        }

        PluginModuleListenerParameters params = (PluginModuleListenerParameters)listenerParameters;
        String addonUserKey = addonRegistry.getUserKey(params.getPluginKey());

        if (StringUtils.isEmpty(addonUserKey)) {
            return false;
        }

        User addonUser = userAccessor.getUser(addonUserKey);

        return confluencePermissionManager.hasPermission(addonUser, Permission.VIEW, contentEvent.getContent());
    }
}
