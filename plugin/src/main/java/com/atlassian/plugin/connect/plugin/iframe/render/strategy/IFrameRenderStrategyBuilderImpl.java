package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.context.IFrameRenderContextBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public class IFrameRenderStrategyBuilderImpl implements IFrameRenderStrategyBuilder,
        IFrameRenderStrategyBuilder.AddOnUriBuilder, IFrameRenderStrategyBuilder.ModuleUriBuilder,
        IFrameRenderStrategyBuilder.TemplatedBuilder, IFrameRenderStrategyBuilder.InitializedBuilder
{
    private static final String TEMPLATE_PATH = "velocity/";
    private static final String TEMPLATE_GENERIC_BODY = TEMPLATE_PATH + "iframe-body.vm";
    private static final String TEMPLATE_GENERIC_PAGE = TEMPLATE_PATH + "iframe-page.vm";
    private static final String TEMPLATE_PROJECT_ADMIN_TAB = TEMPLATE_PATH + "iframe-page-project-admin.vm";
    private static final String TEMPLATE_DIALOG = TEMPLATE_PATH + "iframe-page-dialog.vm";

    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory;
    private final TemplateRenderer templateRenderer;

    private final Map<String, Object> additionalRenderContext = Maps.newHashMap();

    private String addOnKey;
    private String moduleKey;
    private String template;
    private String urlTemplate;
    private String title;
    private String decorator;
    private Condition condition;
    private String width;
    private String height;

    public IFrameRenderStrategyBuilderImpl(
            final IFrameUriBuilderFactory iFrameUriBuilderFactory,
            final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory,
            final TemplateRenderer templateRenderer)
    {

        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.iFrameRenderContextBuilderFactory = iFrameRenderContextBuilderFactory;
        this.templateRenderer = templateRenderer;
    }

    @Override
    public AddOnUriBuilder addOn(final String key)
    {
        addOnKey = checkNotNull(key);
        return this;
    }

    @Override
    public ModuleUriBuilder module(final String key)
    {
        moduleKey = checkNotNull(key);
        return this;
    }

    @Override
    public TemplatedBuilder genericPageTemplate()
    {
        template = TEMPLATE_GENERIC_PAGE;
        return this;
    }

    @Override
    public TemplatedBuilder genericBodyTemplate()
    {
        template = TEMPLATE_GENERIC_BODY;
        return this;
    }

    @Override
    public TemplatedBuilder dialogTemplate()
    {
        template = TEMPLATE_DIALOG;
        return this;
    }

    @Override
    public TemplatedBuilder projectAdminTabTemplate()
    {
        template = TEMPLATE_PROJECT_ADMIN_TAB;
        return this;
    }

    @Override
    public InitializedBuilder urlTemplate(final String urlTemplate)
    {
        this.urlTemplate = urlTemplate;
        return this;
    }

    @Override
    public InitializedBuilder condition(final Condition condition)
    {
        this.condition = condition;
        return this;
    }

    @Override
    public InitializedBuilder title(final String title)
    {
        this.title = title;
        return this;
    }

    @Override
    public InitializedBuilder decorator(final String decorator)
    {
        this.decorator = decorator;
        return this;
    }

    @Override
    public InitializedBuilder dimensions(String width, String height)
    {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public InitializedBuilder additionalRenderContext(final String key, final Object object)
    {
        this.additionalRenderContext.put(key, object);
        return this;
    }

    @Override
    public InitializedBuilder additionalRenderContext(final Map<String, Object> additionalRenderContext)
    {
        if (additionalRenderContext != null)
        {
            this.additionalRenderContext.putAll(additionalRenderContext);
        }
        return this;
    }

    @Override
    public IFrameRenderStrategy build()
    {
        return new IFrameRenderStrategyImpl(iFrameUriBuilderFactory, iFrameRenderContextBuilderFactory,
                templateRenderer, addOnKey, moduleKey, template, urlTemplate, title, decorator, condition,
                additionalRenderContext, width, height);
    }

    private static class IFrameRenderStrategyImpl implements IFrameRenderStrategy
    {

        private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
        private final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory;
        private final TemplateRenderer templateRenderer;

        private final Map<String, Object> additionalRenderContext;
        private final String addOnKey;
        private final String moduleKey;
        private final String template;
        private final String urlTemplate;
        private final String title;
        private final String width;
        private final String height;
        private final String decorator;
        private final Condition condition;

        private IFrameRenderStrategyImpl(final IFrameUriBuilderFactory iFrameUriBuilderFactory,
                final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory,
                final TemplateRenderer templateRenderer, final String addOnKey, final String moduleKey,
                final String template, final String urlTemplate, final String title, final String decorator,
                final Condition condition, final Map<String, Object> additionalRenderContext, String width,
                String height)
        {
            this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
            this.iFrameRenderContextBuilderFactory = iFrameRenderContextBuilderFactory;
            this.templateRenderer = templateRenderer;
            this.addOnKey = addOnKey;
            this.moduleKey = moduleKey;
            this.template = template;
            this.urlTemplate = urlTemplate;
            this.title = title;
            this.decorator = decorator;
            this.condition = condition;
            this.additionalRenderContext = additionalRenderContext;
            this.width = width;
            this.height = height;
        }

        @Override
        public void render(final ModuleContextParameters moduleContextParameters, final Writer writer)
                throws IOException
        {
            String signedUri = iFrameUriBuilderFactory.builder()
                    .addOn(addOnKey)
                    .module(moduleKey)
                    .urlTemplate(urlTemplate)
                    .context(moduleContextParameters)
                    .signAndBuild();

            Map<String, Object> renderContext = iFrameRenderContextBuilderFactory.builder()
                    .addOn(addOnKey)
                    .module(moduleKey)
                    .iframeUri(signedUri)
                    .decorator(decorator)
                    .title(title)
                    .context(additionalRenderContext)
                    .context("contextParams", moduleContextParameters)
                    .context("width", width)
                    .context("height", height)
                    .build();

            templateRenderer.render(template, renderContext, writer);
        }

        @Override
        public boolean shouldShow(Map<String, Object> conditionContext)
        {
            return condition == null || condition.shouldDisplay(conditionContext);
        }

        @Override
        public void shouldShowOrThrow(final Map<String, Object> conditionContext)
        {
            if (!shouldShow(conditionContext))
            {
                throw new PermissionDeniedException(addOnKey, "Cannot render iframe for this page.");
            }
        }
    }

}
