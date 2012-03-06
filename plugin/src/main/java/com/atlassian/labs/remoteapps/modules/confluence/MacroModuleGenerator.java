package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.Schema;
import com.atlassian.labs.remoteapps.modules.external.StaticSchema;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;

/**
 *
 */
public class MacroModuleGenerator extends AbstractMacroModuleGenerator
{
    private final Plugin plugin;
    public MacroModuleGenerator(SystemInformationService systemInformationService, XhtmlContent xhtmlContent, ApplicationLinkOperationsFactory applicationLinkOperationsFactory, MacroContentManager macroContentManager, I18NBeanFactory i18NBeanFactory, PluginAccessor pluginAccessor,
            PluginRetrievalService pluginRetrievalService)
    {
        super(macroContentManager, xhtmlContent, i18NBeanFactory, applicationLinkOperationsFactory,
                systemInformationService, pluginAccessor);
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
        return new StorageFormatMacro(remoteMacroInfo, xhtmlContent, macroContentManager);
    }

}
