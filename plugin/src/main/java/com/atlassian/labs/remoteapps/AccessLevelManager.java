package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.labs.remoteapps.descriptor.external.AccessLevel;
import com.atlassian.labs.remoteapps.descriptor.external.AccessLevelModuleDescriptor;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScopeModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class AccessLevelManager implements DisposableBean
{
    private final PluginModuleTracker<AccessLevel, AccessLevelModuleDescriptor> accessLevelTracker;

    @Autowired
    public AccessLevelManager(PluginEventManager pluginEventManager, PluginAccessor pluginAccessor)
    {
        this.accessLevelTracker = new DefaultPluginModuleTracker<AccessLevel, AccessLevelModuleDescriptor>(pluginAccessor, pluginEventManager,
                AccessLevelModuleDescriptor.class);
    }

    public AccessLevel getAccessLevel(String value)
    {
        for (AccessLevel level : accessLevelTracker.getModules())
        {
            if (level.getId().equals(value))
            {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid access level '" + value + "'");
    }

    @Override
    public void destroy() throws Exception
    {
        accessLevelTracker.close();
    }
}
