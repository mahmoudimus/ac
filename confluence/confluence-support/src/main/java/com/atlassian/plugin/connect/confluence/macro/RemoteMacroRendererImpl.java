package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil;
import com.atlassian.plugin.connect.api.web.iframe.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.modules.beans.nested.EmbeddedStaticContentMacroBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModesBean;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static com.atlassian.plugin.connect.confluence.macro.DynamicContentMacroModuleProvider.CONTENT_CLASSIFIER;

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
    public String executeDynamic(String addonKey, String moduleKey, MacroRenderModesBean renderModes,
                                 Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext) throws MacroExecutionException
    {
        // ACDEV-1705 null check on render modes, will be null if none are specified
        EmbeddedStaticContentMacroBean fallback = renderModes == null ? null :
                renderModes.getEmbeddedStaticContentMacro(conversionContext.getOutputType());

        if (fallback != null)
        {
            log.debug("execute dynamic macro [ {} ] from add on [ {} ] with render mode [ {} ] to device [ {} ] to fallback [ {} ]",
                    new Object[]{moduleKey, addonKey, conversionContext.getOutputType(), conversionContext.getOutputDeviceType(), fallback.getUrl()});
            return executeStatic(addonKey, moduleKey, fallback.getUrl(), parameters, storageFormatBody, conversionContext);
        }
        else
        {
            log.debug("execute dynamic macro [ {} ] from add on [ {} ] with render mode [ {} ] to device [ {} ] without fallback",
                    new Object[]{moduleKey, addonKey, conversionContext.getOutputType(), conversionContext.getOutputDeviceType()});
            IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKey, moduleKey, CONTENT_CLASSIFIER);
            ModuleContextParameters moduleContext = macroModuleContextExtractor.extractParameters(storageFormatBody, conversionContext, parameters);
            return IFrameRenderStrategyUtil.renderToString(moduleContext, renderStrategy);
        }
    }

    @Override
    public String executeStatic(String addonKey, String moduleKey, String uriTemplate,
                                Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext)
            throws MacroExecutionException
    {
        ModuleContextParameters moduleContext = macroModuleContextExtractor.extractParameters(
                storageFormatBody,
                conversionContext,
                parameters
        );

        String uri = iFrameUriBuilderFactory.builder()
                .addon(addonKey)
                .namespace(moduleKey)
                .urlTemplate(uriTemplate)
                .context(moduleContext)
                .sign(false)
                .build();

        try
        {
            return macroContentManager.getStaticContent(HttpMethod.GET, URI.create(uri),
                    Collections.<String, String[]>emptyMap(), conversionContext,
                    remotablePluginAccessorFactory.get(addonKey));
        }
        catch (Exception e)
        {
            logError(addonKey, e, conversionContext.getEntity(), uri);
            throw new MacroExecutionException(e);
        }
    }

    private void logError(String addonKey, Exception e, ContentEntityObject entity, String uri)
    {
        String context = "Add-On: " + addonKey + ", Entity: " + entity.getTitle() + ", URL: " + uri;

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
