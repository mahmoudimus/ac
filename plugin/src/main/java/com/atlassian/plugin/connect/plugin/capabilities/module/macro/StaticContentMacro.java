package com.atlassian.plugin.connect.plugin.capabilities.module.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.Format;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.RequiresFormat;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

public class StaticContentMacro extends AbstractMacro
{
    private static final Logger log = LoggerFactory.getLogger(StaticContentMacro.class);

    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final MacroModuleContextExtractor macroModuleContextExtractor;
    private final MacroContentManager macroContentManager;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;

    private final String addOnKey;
    private final String moduleKey;
    private final String uriTemplate;

    public StaticContentMacro(String addOnKey, String moduleKey, String uriTemplate, BodyType bodyType,
                              OutputType outputType, IFrameUriBuilderFactory iFrameUriBuilderFactory,
                              MacroModuleContextExtractor macroModuleContextExtractor, MacroContentManager macroContentManager,
                              RemotablePluginAccessorFactory remotablePluginAccessorFactory)
    {
        super(bodyType, outputType);
        this.addOnKey = addOnKey;
        this.moduleKey = moduleKey;
        this.uriTemplate = uriTemplate;
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.macroModuleContextExtractor = macroModuleContextExtractor;
        this.macroContentManager = macroContentManager;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
    }

    @Override
    @RequiresFormat(Format.Storage)
    public String execute(Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext)
            throws MacroExecutionException
    {
        ModuleContextParameters moduleContext = macroModuleContextExtractor.extractParameters(
                storageFormatBody,
                conversionContext,
                parameters
        );

        String uri = iFrameUriBuilderFactory.builder()
                .addOn(addOnKey)
                .namespace(moduleKey)
                .urlTemplate(uriTemplate)
                .context(moduleContext)
                .sign(false)
                .build();

        try
        {
            return macroContentManager.getStaticContent(HttpMethod.GET, URI.create(uri),
                    Collections.<String, String[]>emptyMap(), conversionContext,
                    remotablePluginAccessorFactory.getOrThrow(addOnKey));
        }
        catch (Exception e)
        {
            logError(e, conversionContext.getEntity(), uri);
            throw new MacroExecutionException(e);
        }
    }

    private void logError(Exception e, ContentEntityObject entity, String uri)
    {
        String context = "Add-On: "
                + addOnKey
                + ", Entity: "
                + entity.getTitle()
                + ", URL: "
                + uri;

        if (e instanceof SocketTimeoutException)
        {
            log.warn("Timeout retrieving add-on macro content. " + context);
        }
        else
        {
            log.error("Error retrieving add-on macro content. " + context, e);
        }
    }
}
