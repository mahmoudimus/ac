package com.atlassian.plugin.connect.plugin.capabilities.module.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModeType;
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
    public String executeDynamic(String addOnKey, String moduleKey, Map<MacroRenderModeType, String> renderModeUriTemplates,
                                 Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext) throws MacroExecutionException
    {
        if(log.isDebugEnabled()) {
            log.info("execute dynamic macro [ " + moduleKey + " ] from add on [ " + addOnKey + " ] with render mode [ " + conversionContext.getOutputType() + " ] to device [ " + conversionContext.getOutputDeviceType() + " ]");
        }

        MacroRenderModeType macroRenderModeType = null;
        try
        {
            macroRenderModeType = MacroRenderModeType.valueOf(conversionContext.getOutputType().toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            // Confluence sends these enums as their string value so we
            // need to be resilient to the scope of these values changing
        }

        if (macroRenderModeType != null && renderModeUriTemplates.containsKey(macroRenderModeType))
        {
            return executeStatic(addOnKey, moduleKey, renderModeUriTemplates.get(macroRenderModeType), parameters, storageFormatBody, conversionContext);
        }
        else if (macroRenderModeType != null && renderModeUriTemplates.containsKey(MacroRenderModeType.STATIC) && macroRenderModeType.isStatic())
        {
            return executeStatic(addOnKey, moduleKey, renderModeUriTemplates.get(MacroRenderModeType.STATIC), parameters, storageFormatBody, conversionContext);
        }
        else
        {
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
