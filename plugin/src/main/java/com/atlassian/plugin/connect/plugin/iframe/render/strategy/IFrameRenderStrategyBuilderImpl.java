package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.context.IFrameRenderContextBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.templaterenderer.TemplateRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public class IFrameRenderStrategyBuilderImpl implements IFrameRenderStrategyBuilder,
        IFrameRenderStrategyBuilder.AddOnUriBuilder, IFrameRenderStrategyBuilder.ModuleUriBuilder, IFrameRenderStrategyBuilder.TemplatedBuilder,
        IFrameRenderStrategyBuilder.InitializedBuilder
{
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory;
    private final TemplateRenderer templateRenderer;

    private String addOnKey;
    private String moduleKey;
    private String template;
    private String urlTemplate;
    private String title;
    private String decorator;
    private Condition condition;

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
    public TemplatedBuilder template(final String templatePath)
    {
        template = checkNotNull(templatePath);
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
    public IFrameRenderStrategy build()
    {
        return new IFrameRenderStrategyImpl(iFrameUriBuilderFactory, iFrameRenderContextBuilderFactory,
                templateRenderer, addOnKey, moduleKey, template, urlTemplate, title, decorator, condition);
    }

    private static class IFrameRenderStrategyImpl implements IFrameRenderStrategy
    {

        private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
        private final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory;
        private final TemplateRenderer templateRenderer;

        private final String addOnKey;
        private final String moduleKey;
        private final String template;
        private final String urlTemplate;
        private final String title;
        private final String decorator;
        private final Condition condition;

        private IFrameRenderStrategyImpl(final IFrameUriBuilderFactory iFrameUriBuilderFactory,
                final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory,
                final TemplateRenderer templateRenderer, final String addOnKey,
                final String moduleKey, final String template, final String urlTemplate, final String title,
                final String decorator, final Condition condition)
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
        }

        @Override
        public void render(final ModuleContextParameters moduleContextParameters, final OutputStream outputStream)
                throws IOException
        {
            if (condition != null && !condition.shouldDisplay(Collections.<String, Object>emptyMap()))
            {
                throw new PermissionDeniedException(addOnKey, "Cannot render iframe for this page.");
            }

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
                    .build();

            templateRenderer.render(template, renderContext, new OutputStreamWriter(outputStream));
        }

    }

}
