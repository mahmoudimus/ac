package com.atlassian.plugin.connect.plugin.module.applinks;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.descriptors.CannotDisable;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredElementText;

/**
 * Dynamically creates an application link for a plugin host
 */
//TODO: do we really need this? maybe we can just create the app links somewhere else.
@CannotDisable
public final class RemotePluginContainerModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final ConnectApplinkManager connectApplinkManager;
    private final ConnectAddOnUserService connectAddOnUserService;

    private static final Logger log = LoggerFactory.getLogger(RemotePluginContainerModuleDescriptor.class);
    
    private String addonBaseUrl;
    
    public RemotePluginContainerModuleDescriptor(ConnectApplinkManager connectApplinkManager, ConnectAddOnUserService connectAddOnUserService)
    {
        super(ModuleFactory.LEGACY_MODULE_FACTORY);
        this.connectApplinkManager = connectApplinkManager;
        this.connectAddOnUserService = connectAddOnUserService;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        Element oauthElement = element.element("oauth");
        String displayUrl = getRequiredAttribute(element, "display-url");

        if (null != element.getParent() && element.getParent().elements(element.getName()).size() > 1)
        {
            throw new PluginParseException("Can only have one remote-plugin-container module in a descriptor");
        }

        this.addonBaseUrl = displayUrl;
        
        if(null != oauthElement)
        {
            String publicKey = getRequiredElementText(oauthElement, "public-key");
            connectApplinkManager.createAppLink(plugin,displayUrl,AuthenticationType.OAUTH,publicKey,"");
        }
        
        
    }

    public String getAddonBaseUrl()
    {
        return addonBaseUrl;    
    }
    
    @Override
    public void enabled()
    {
        super.enabled();
    }

    @Override
    public void disabled()
    {
        super.disabled();
    }

    @Override
    public Void getModule()
    {
        return null;
    }
}
