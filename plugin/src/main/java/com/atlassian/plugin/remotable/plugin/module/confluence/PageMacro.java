package com.atlassian.plugin.remotable.plugin.module.confluence;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.remotable.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.remotable.spi.RemotablePluginAccessor;
import com.atlassian.plugin.remotable.spi.module.IFrameContext;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;

public final class PageMacro extends AbstractRemoteMacro
{
    private final UserManager userManager;
    private final IFrameContext iframeContext;
    private final IFrameRendererImpl iFrameRenderer;
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;

    public PageMacro(RemoteMacroInfo remoteMacroInfo, UserManager userManager,
            IFrameRendererImpl iFrameRenderer, IFrameContext iframeContext,
            DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory
    )
    {
        super(remotablePluginAccessorFactory, remoteMacroInfo);
        this.userManager = userManager;
        this.iframeContext = iframeContext;
        this.iFrameRenderer = iFrameRenderer;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
    }

    @Override
    public String execute(Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext) throws MacroExecutionException
    {
        String remoteUser = userManager.getRemoteUsername();
        String counter = incrementCounter(conversionContext);
        try
        {
            MacroInstance macroInstance = new MacroInstance(
                    conversionContext,
                    remoteMacroInfo.getUrl(),
                    storageFormatBody,
                    parameters,
                    remoteMacroInfo.getRequestContextParameterFactory(),
                    remotablePluginAccessorFactory.get(remoteMacroInfo.getPluginKey()));
             return iFrameRenderer.render(
                     new IFrameContextImpl(iframeContext, "-" + counter),
                     "",
                     convertParams(macroInstance.getUrlParameters(userManager.getRemoteUsername())),
                     remoteUser);

        } catch (IOException e)
        {
            throw new MacroExecutionException(e);
        }
    }

    private String incrementCounter(ConversionContext ctx)
    {
        String key = "__counter_" + iframeContext.getNamespace();
        Integer counter = (Integer) ctx.getProperty(key);
        counter = counter == null ? 0 : counter+1;
        ctx.setProperty(key, counter);

        return counter.toString();
    }

    private Map<String, String[]> convertParams(Map<String, String> parameters)
    {
        return Maps.transformValues(parameters, new Function<String, String[]>()
        {

            @Override
            public String[] apply(String from)
            {
                return new String[]{from};
            }
        });
    }

    @Override
    public RemotablePluginAccessor getRemotablePluginAccessor(String pluginKey)
    {
        return remotablePluginAccessorFactory.get(pluginKey);
    }
}
