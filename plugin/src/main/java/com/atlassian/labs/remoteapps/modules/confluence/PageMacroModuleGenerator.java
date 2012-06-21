package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.IFrameParams;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.Schema;
import com.atlassian.labs.remoteapps.modules.external.StaticSchema;
import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
import com.atlassian.labs.remoteapps.util.contextparameter.ContextParameterParser;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.sal.api.user.UserManager;

/**
 *
 */
public class PageMacroModuleGenerator extends AbstractMacroModuleGenerator
{
    private final Plugin plugin;

    public PageMacroModuleGenerator(SystemInformationService systemInformationService,
            ApplicationLinkOperationsFactory applicationLinkOperationsFactory,
            MacroContentManager macroContentManager, I18NBeanFactory i18NBeanFactory,
            PluginAccessor pluginAccessor, UserManager userManager, IFrameRenderer iFrameRenderer,
            PluginRetrievalService pluginRetrievalService, HostContainer hostContainer,
            ServletModuleManager servletModuleManager,
            ContextParameterParser contextParameterParser)
    {
        super(macroContentManager, i18NBeanFactory, applicationLinkOperationsFactory,
                systemInformationService, pluginAccessor, hostContainer, servletModuleManager,
                contextParameterParser, iFrameRenderer, userManager);
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "page-macro";
    }

    @Override
    public String getName()
    {
        return "Page Macro";
    }

    @Override
    public String getDescription()
    {
        return "A Confluence macro that loads the remote page as an IFrame";
    }

    @Override
    public Schema getSchema()
    {
        return new StaticSchema(plugin,
                "page-macro.xsd",
                "/xsd/page-macro.xsd",
                "PageMacroType");
    }

    @Override
    protected RemoteMacro createMacro(RemoteMacroInfo remoteMacroInfo, RemoteAppCreationContext ctx)
    {
        String moduleKey = remoteMacroInfo.getElement().attributeValue("key");
        IFrameParams params = new IFrameParams(remoteMacroInfo.getElement());
        IFrameContext iFrameContext = new IFrameContext(
                remoteMacroInfo.getApplicationLinkOperations(),
                remoteMacroInfo.getUrl(),
                moduleKey,
                params
                );
        return new PageMacro(remoteMacroInfo, userManager, iFrameRenderer, iFrameContext);
    }

}
