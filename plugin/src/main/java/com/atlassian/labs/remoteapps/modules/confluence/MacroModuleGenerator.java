package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.plugin.PluginAccessor;

/**
 *
 */
public class MacroModuleGenerator extends AbstractMacroModuleGenerator
{

    public MacroModuleGenerator(SystemInformationService systemInformationService, XhtmlContent xhtmlContent, ApplicationLinkOperationsFactory applicationLinkOperationsFactory, MacroContentManager macroContentManager, I18NBeanFactory i18NBeanFactory, PluginAccessor pluginAccessor)
    {
        super(macroContentManager, xhtmlContent, i18NBeanFactory, applicationLinkOperationsFactory,
                systemInformationService, pluginAccessor);
    }

    @Override
    public String getType()
    {
        return "macro";
    }

    @Override
    protected RemoteMacro createMacro(RemoteMacroInfo remoteMacroInfo, RemoteAppCreationContext ctx)
    {
        return new StorageFormatMacro(remoteMacroInfo, xhtmlContent, macroContentManager);
    }

}
