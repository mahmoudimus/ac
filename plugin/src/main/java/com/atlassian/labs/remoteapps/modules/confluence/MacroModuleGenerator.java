package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.labs.remoteapps.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.Schema;
import com.atlassian.labs.remoteapps.modules.external.StaticSchema;
import com.atlassian.labs.remoteapps.util.contextparameter.ContextParameterParser;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.user.UserManager;

/**
 *
 */
public class MacroModuleGenerator extends AbstractMacroModuleGenerator
{
    private final Plugin plugin;
    private final WebResourceManager webResourceManager;
    public MacroModuleGenerator(SystemInformationService systemInformationService,
            MacroContentManager macroContentManager, I18NBeanFactory i18NBeanFactory,
            PluginAccessor pluginAccessor,
            PluginRetrievalService pluginRetrievalService, HostContainer hostContainer,
            ServletModuleManager servletModuleManager,
            IFrameRenderer iFrameRenderer, UserManager userManager,
            WebResourceManager webResourceManager,
            ContextParameterParser contextParameterParser)
    {
        super(macroContentManager, i18NBeanFactory,
                systemInformationService, pluginAccessor, hostContainer, servletModuleManager,
                contextParameterParser, iFrameRenderer, userManager);


        this.webResourceManager = webResourceManager;
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "macro";
    }

    @Override
    public String getName()
    {
        return "Macro";
    }

    @Override
    public String getDescription()
    {
        return "A Confluence macro that returns XHTML in the Confluence storage format to be cached for at least one hour";
    }

    @Override
    public Schema getSchema()
    {
        return new StaticSchema(plugin,
                "macro.xsd",
                "/xsd/macro.xsd",
                "MacroType");
    }

    @Override
    protected RemoteMacro createMacro(RemoteMacroInfo remoteMacroInfo, RemoteAppCreationContext ctx)
    {
        return new StorageFormatMacro(remoteMacroInfo, macroContentManager,
                webResourceManager);
    }

}
