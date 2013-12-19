package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.uri.Uri;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public final class DynamicContentMacro extends AbstractContentMacro
{
    private final DynamicContentMacroModuleBean macroBean;
    private final IFrameRenderer iFrameRenderer;

    public DynamicContentMacro(String pluginKey,
                               DynamicContentMacroModuleBean macroBean,
                               UserManager userManager,
                               IFrameRenderer iFrameRenderer,
                               RemotablePluginAccessorFactory remotablePluginAccessorFactory,
                               UrlVariableSubstitutor urlVariableSubstitutor)
    {
        super(pluginKey, macroBean, userManager, remotablePluginAccessorFactory, urlVariableSubstitutor);
        this.macroBean = macroBean;
        this.iFrameRenderer = iFrameRenderer;
    }

    @Override
    public String execute(Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext) throws MacroExecutionException
    {
        String counter = incrementCounter(conversionContext);
        try
        {
            MacroContext macroContext = new MacroContext(conversionContext);
            Uri uri = resolveUrlTemplate(macroContext.getParameters());

            IFrameContext iFrameContext = new IFrameContextImpl(getPluginKey(), uri.getPath(), getNamespace(counter), getIFrameParams());
            MacroRequestParameters macroParameters = new MacroRequestParameters.Builder()
                    .withSingleValueParameters(parameters)
                    .withMultiValueParameters(uri.getQueryParameters())
                    .withBody(storageFormatBody)
                    .withUser(getUser())
                    .build();

            if (getOutputType().equals(OutputType.INLINE))
            {
                return iFrameRenderer.renderInline(iFrameContext, "", macroParameters.getQueryParameters(),
                        getUsername(), Collections.<String, Object>emptyMap());
            }
            else
            {
                return iFrameRenderer.render(iFrameContext, "", macroParameters.getQueryParameters(),
                        getUsername(), Collections.<String, Object>emptyMap());
            }
        }
        catch (IOException e)
        {
            throw new MacroExecutionException(e);
        }
        catch (Uri.UriException e)
        {
            throw new MacroExecutionException(e);
        }
    }

    private String getNamespace(String counter)
    {
        return macroBean.getKey() + "-" + counter;
    }

    private IFrameParams getIFrameParams()
    {
        IFrameParams iFrameParams = new IFrameParamsImpl();
        if (null != macroBean.getWidth())
        {
            iFrameParams.setParam("width", macroBean.getWidth());
        }
        if (null != macroBean.getHeight())
        {
            iFrameParams.setParam("height", macroBean.getHeight());
        }
        return iFrameParams;
    }

    private String incrementCounter(ConversionContext ctx)
    {
        String key = "__counter_" + macroBean.getKey();
        Integer counter = (Integer) ctx.getProperty(key);
        counter = null == counter ? 0 : counter + 1;
        ctx.setProperty(key, counter);

        return counter.toString();
    }
}
