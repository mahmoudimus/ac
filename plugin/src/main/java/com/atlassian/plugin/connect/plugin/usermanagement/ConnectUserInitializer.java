package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.predicate.PluginPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class ConnectUserInitializer implements InitializingBean
{
    private final ConnectAddOnUserService connectAddOnUserService;
    private final PluginAccessor pluginAccessor;
    private final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;

    private static final Logger log = LoggerFactory.getLogger(ConnectUserInitializer.class);
    private final ConnectAddonRegistry connectAddonRegistry;

    @Autowired
    public ConnectUserInitializer(ConnectAddOnUserService connectAddOnUserService,
                                  PluginAccessor pluginAccessor,
                                  JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService,
                                  ConnectAddonRegistry connectAddonRegistry)
    {
        this.connectAddonRegistry = checkNotNull(connectAddonRegistry);
        this.connectAddOnUserService = checkNotNull(connectAddOnUserService);
        this.pluginAccessor = checkNotNull(pluginAccessor);
        this.jsonConnectAddOnIdentifierService = checkNotNull(jsonConnectAddOnIdentifierService);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        for (Plugin plugin : pluginAccessor.getPlugins(thatAreConnectAddOns()))
        {
            initAddOnUser(plugin);
        }
    }

    private void initAddOnUser(Plugin connectAddOn)
    {
        try
        {
            String descriptor = connectAddonRegistry.getDescriptor(connectAddOn.getKey());

            if (null == descriptor)
            {
                log.error(String.format("Failed to ensure that the user exists for Connect add-on '%s' because it has no descriptor in the Connect registry. It will not be able to use the APIs. Uninstalling and reinstalling it will re-attempt this operation.", connectAddOn.getKey()));
            }
            else
            {
                connectAddOnUserService.getOrCreateUserKey(connectAddOn.getKey(), connectAddOn.getName());
            }
        }
        catch (ConnectAddOnUserInitException e)
        {
            log.error(String.format("Failed to ensure that the user exists for Connect add-on '%s' on Connect startup. It will not be able to use the APIs. Uninstalling and reinstalling it will re-attempt this operation. Ignoring so as not to interfere with other add-ons...", connectAddOn.getKey()), e);
        }
    }

    private PluginPredicate thatAreConnectAddOns()
    {
        return new PluginPredicate()
            {
                @Override
                public boolean matches(Plugin plugin)
                {
                    // no need to check the legacy xml add-ons because they are deprecated and still function using impersonation
                    return jsonConnectAddOnIdentifierService.isConnectAddOn(plugin);
                }
            };
    }
}
