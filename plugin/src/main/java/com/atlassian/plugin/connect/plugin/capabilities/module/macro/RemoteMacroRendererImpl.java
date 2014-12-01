package com.atlassian.plugin.connect.plugin.capabilities.module.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.modules.beans.nested.EmbeddedStaticContentMacroBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModesBean;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyUtil;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.provider.DynamicContentMacroModuleProvider.CONTENT_CLASSIFIER;

@ConfluenceComponent
public class RemoteMacroRendererImpl implements RemoteMacroRenderer
{
    private static final Logger log = LoggerFactory.getLogger(RemoteMacroRendererImpl.class);

    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final MacroModuleContextExtractor macroModuleContextExtractor;
    private final MacroContentManager macroContentManager;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Autowired
    public RemoteMacroRendererImpl(
            IFrameUriBuilderFactory iFrameUriBuilderFactory,
            MacroModuleContextExtractor macroModuleContextExtractor, MacroContentManager macroContentManager,
            RemotablePluginAccessorFactory remotablePluginAccessorFactory, IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.macroModuleContextExtractor = macroModuleContextExtractor;
        this.macroContentManager = macroContentManager;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public String executeDynamic(String addOnKey, String moduleKey, MacroRenderModesBean renderModes,
                                 Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext) throws MacroExecutionException
    {
        EmbeddedStaticContentMacroBean fallback = renderModes.getEmbeddedStaticContentMacro(conversionContext.getOutputType());

        if (fallback != null)
        {
            log.debug("execute dynamic macro [ {} ] from add on [ {} ] with render mode [ {} ] to device [ {} ] to fallback [ {} ]",
                    new Object[]{moduleKey,addOnKey,conversionContext.getOutputType(),conversionContext.getOutputDeviceType(), fallback.getUrl()});
            return executeStatic(addOnKey, moduleKey, fallback.getUrl(), parameters, storageFormatBody, conversionContext);
        }
        else
        {
            log.debug("execute dynamic macro [ {} ] from add on [ {} ] with render mode [ {} ] to device [ {} ] without fallback",
                    new Object[]{moduleKey,addOnKey,conversionContext.getOutputType(),conversionContext.getOutputDeviceType()});
            IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addOnKey, moduleKey, CONTENT_CLASSIFIER);
            ModuleContextParameters moduleContext = macroModuleContextExtractor.extractParameters(storageFormatBody, conversionContext, parameters);
            return IFrameRenderStrategyUtil.renderToString(moduleContext, renderStrategy);
        }
    }

    @Override
    public String executeStatic(String addOnKey, String moduleKey, String uriTemplate,
                                Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext)
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
            logError(addOnKey, e, conversionContext.getEntity(), uri);
            throw new MacroExecutionException(e);
        }
    }

    private void logError(String addOnKey, Exception e, ContentEntityObject entity, String uri)
    {
        String context = "Add-On: " + addOnKey + ", Entity: " + entity.getTitle() + ", URL: " + uri;

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
