package com.atlassian.plugin.connect.plugin.capabilities.module;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Map;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.Format;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.RequiresFormat;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.uri.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticContentMacro extends AbstractContentMacro
{
    private static final Logger log = LoggerFactory.getLogger(StaticContentMacro.class);

    private final StaticContentMacroModuleBean macroBean;
    private final MacroContentManager macroContentManager;

    public StaticContentMacro(String pluginKey,
                              StaticContentMacroModuleBean macroBean,
                              UserManager userManager,
                              MacroContentManager macroContentManager,
                              RemotablePluginAccessorFactory remotablePluginAccessorFactory,
                              UrlVariableSubstitutor urlVariableSubstitutor)
    {
        super(pluginKey, macroBean, userManager, remotablePluginAccessorFactory, urlVariableSubstitutor);
        this.macroBean = macroBean;
        this.macroContentManager = macroContentManager;
    }

    @Override
    @RequiresFormat(Format.Storage)
    public String execute(Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext) throws MacroExecutionException
    {
        final RemotablePluginAccessor remotablePluginAccessor = getRemotablePluginAccessor();
        Uri uri = null;
        try
        {
            MacroContext macroContext = new MacroContext(conversionContext, storageFormatBody, getUser());
            uri = resolveUrlTemplate(macroContext.getParameters());

            MacroRequestParameters macroParameters = new MacroRequestParameters.Builder()
                    .withMacroParameters(parameters)
                    .withURLParameters(uri.getQueryParameters())
                    .build();

            return macroContentManager.getStaticContent(HttpMethod.valueOf(macroBean.getMethod().name()), new URI(uri.getPath()),
                    macroParameters.getSingleQueryParameters(), conversionContext, remotablePluginAccessor);
        }
        catch (Exception e)
        {
            logError(e, conversionContext.getEntity().getTitle(), uri.toString());
            throw new MacroExecutionException(e);
        }
    }

    private void logError(Exception e, String entityTitle, String url)
    {
        String context = "Add-On: "
            + getRemotablePluginAccessor().getName()
            + ", Entity: "
            + entityTitle
            + ", URL: "
            + url;

        if (e instanceof SocketTimeoutException)
        {
            log.warn("Timeout retrieving add-on macro content. " + context);
        }
        else {
            log.error("Error retrieving add-on macro content. " + context, e);
        }
    }
}
