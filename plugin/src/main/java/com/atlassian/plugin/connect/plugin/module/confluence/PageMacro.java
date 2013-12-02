package com.atlassian.plugin.connect.plugin.module.confluence;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;

import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

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
        String counter = incrementCounter(conversionContext);
        try
        {
            MacroInstance macroInstance = new MacroInstance(
                    conversionContext,
                    remoteMacroInfo.getUrl(),
                    remoteMacroInfo.getHttpMethod(),
                    storageFormatBody,
                    parameters,
                    remoteMacroInfo.getRequestContextParameterFactory(),
                    remotablePluginAccessorFactory.get(remoteMacroInfo.getPluginKey()));

            UserProfile user = userManager.getRemoteUser();
            String username = user == null ? "" : user.getUsername();
            String userKey = user == null ? "" : user.getUserKey().getStringValue();
            IFrameContextImpl iframeContextImpl = new IFrameContextImpl(iframeContext, "-" + counter);
            Map<String, String[]> queryParams = convertParams(macroInstance.getUrlParameters(username, userKey));

            if (getOutputType().equals(OutputType.INLINE)){
                return iFrameRenderer.renderInline(iframeContextImpl, "", queryParams, username, Collections.<String, Object>emptyMap());
            } else {
                return iFrameRenderer.render(iframeContextImpl, "", queryParams, username, Collections.<String, Object>emptyMap());
            }

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
