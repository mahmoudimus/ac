package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.IFrameParams;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.Schema;
import com.atlassian.labs.remoteapps.modules.external.StaticSchema;
import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.user.UserManager;

/**
 *
 */
public class PageMacroModuleGenerator extends AbstractMacroModuleGenerator
{
    private final UserManager userManager;
    private final IFrameRenderer iFrameRenderer;
    private final Plugin plugin;
    public PageMacroModuleGenerator(SystemInformationService systemInformationService,
            XhtmlContent xhtmlContent,
            ApplicationLinkOperationsFactory applicationLinkOperationsFactory,
            MacroContentManager macroContentManager, I18NBeanFactory i18NBeanFactory,
            PluginAccessor pluginAccessor, UserManager userManager, IFrameRenderer iFrameRenderer,
            PluginRetrievalService pluginRetrievalService)
    {
        super(macroContentManager, xhtmlContent, i18NBeanFactory, applicationLinkOperationsFactory,
                systemInformationService, pluginAccessor);
        this.userManager = userManager;
        this.iFrameRenderer = iFrameRenderer;
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
