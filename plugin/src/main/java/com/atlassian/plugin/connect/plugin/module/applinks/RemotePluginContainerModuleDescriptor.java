package com.atlassian.plugin.connect.plugin.module.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.applinks.DefaultConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserService;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.util.BundleUtil;
import com.atlassian.plugin.connect.plugin.util.OsgiServiceUtils;
import com.atlassian.plugin.connect.spi.Permissions;
import com.atlassian.plugin.connect.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.descriptors.CannotDisable;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.List;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.*;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dynamically creates an application link for a plugin host
 */
//TODO: do we really need this? maybe we can just create the app links somewhere else.
@CannotDisable
public final class RemotePluginContainerModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final ConnectApplinkManager connectApplinkManager;
    private final ConnectAddOnUserService connectAddOnUserService;
    private final ConnectAddonRegistry connectAddonRegistry;

    private static final Logger log = LoggerFactory.getLogger(RemotePluginContainerModuleDescriptor.class);
    
    public RemotePluginContainerModuleDescriptor(ConnectApplinkManager connectApplinkManager, ConnectAddOnUserService connectAddOnUserService, ConnectAddonRegistry connectAddonRegistry)
    {
        super(ModuleFactory.LEGACY_MODULE_FACTORY);
        this.connectApplinkManager = connectApplinkManager;
        this.connectAddOnUserService = connectAddOnUserService;
        this.connectAddonRegistry = connectAddonRegistry;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        Element oauthElement = element.element("oauth");
        String displayUrl = getRequiredAttribute(element, "display-url");
        String publicKey = getRequiredElementText(oauthElement, "public-key");

        if (null != element.getParent() && element.getParent().elements(element.getName()).size() > 1)
        {
            throw new PluginParseException("Can only have one remote-plugin-container module in a descriptor");
        }
        
        connectApplinkManager.createAppLink(plugin,displayUrl,AuthenticationType.OAUTH,publicKey,"");
        connectAddonRegistry.storeBaseUrl(plugin.getKey(), displayUrl);
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
