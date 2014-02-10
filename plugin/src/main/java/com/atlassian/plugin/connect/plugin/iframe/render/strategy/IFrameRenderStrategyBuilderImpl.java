package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import javax.annotation.Nullable;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleViewParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.context.IFrameRenderContextBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
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
    private static final String TEMPLATE_GENERIC_INLINE = TEMPLATE_PATH + "iframe-body-inline.vm";
    private static final String TEMPLATE_PAGE = TEMPLATE_PATH + "iframe-page.vm";
    private static final String TEMPLATE_PROJECT_ADMIN_TAB = TEMPLATE_PATH + "iframe-page-project-admin.vm";
    private static final String TEMPLATE_WORKFLOW_POSTFUNCTION = TEMPLATE_PATH + "jira/workflow/iframe-post-function.vm";

    private static final String TEMPLATE_ACCESS_DENIED_PAGE = TEMPLATE_PATH + "iframe-page-accessdenied.vm";
    private static final String TEMPLATE_ACCESS_DENIED_GENERIC_BODY = TEMPLATE_PATH + "iframe-body-accessdenied.vm";

    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory;
    private final TemplateRenderer templateRenderer;

    private final Map<String, Object> additionalRenderContext = Maps.newHashMap();

    private String addOnKey;
    private String moduleKey;
    private String template;
    private String accessDeniedTemplate;
    private String urlTemplate;
    private String title;
    private String decorator;
    private Condition condition;
    private String width;
    private String height;
    private boolean uniqueNamespace;

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
    public TemplatedBuilder pageTemplate()
    {
        template = TEMPLATE_PAGE;
        accessDeniedTemplate = TEMPLATE_ACCESS_DENIED_PAGE;
        return this;
    }

    @Override
    public TemplatedBuilder genericBodyTemplate()
    {
        template = TEMPLATE_GENERIC_BODY;
        accessDeniedTemplate = TEMPLATE_ACCESS_DENIED_GENERIC_BODY;
        return this;
    }

    @Override
    public TemplatedBuilder genericBodyTemplate(boolean inline)
    {
        template = inline ? TEMPLATE_GENERIC_INLINE : TEMPLATE_GENERIC_BODY;
        accessDeniedTemplate = TEMPLATE_ACCESS_DENIED_GENERIC_BODY;
        return this;
    }

    @Override
    public TemplatedBuilder dialogTemplate()
    {
        template = TEMPLATE_GENERIC_BODY;
        accessDeniedTemplate = TEMPLATE_ACCESS_DENIED_GENERIC_BODY;
        return this;
    }

    @Override
    public TemplatedBuilder projectAdminTabTemplate()
    {
        template = TEMPLATE_PROJECT_ADMIN_TAB;
        accessDeniedTemplate = TEMPLATE_ACCESS_DENIED_PAGE;
        return this;
    }

    @Override
    public TemplatedBuilder workflowPostFunctionTemplate()
    {
        template = TEMPLATE_WORKFLOW_POSTFUNCTION;
        accessDeniedTemplate = TEMPLATE_ACCESS_DENIED_GENERIC_BODY;
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
    public InitializedBuilder ensureUniqueNamespace(boolean uniqueNamespace)
    {
        this.uniqueNamespace = uniqueNamespace;
        return this;
    }

    @Override
    public IFrameRenderStrategy build()
    {
        return new IFrameRenderStrategyImpl(iFrameUriBuilderFactory, iFrameRenderContextBuilderFactory,
                templateRenderer, addOnKey, moduleKey, template, accessDeniedTemplate, urlTemplate, title,
                decorator, condition, additionalRenderContext, width, height, uniqueNamespace);
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
        private final String accessDeniedTemplate;
        private final String urlTemplate;
        private final String title;
        private final String width;
        private final String height;
        private final boolean uniqueNamespace;
        private final String decorator;
        private final Condition condition;

        private IFrameRenderStrategyImpl(final IFrameUriBuilderFactory iFrameUriBuilderFactory,
                final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory,
                final TemplateRenderer templateRenderer, final String addOnKey, final String moduleKey,
                final String template, final String accessDeniedTemplate, final String urlTemplate,
                final String title, final String decorator, final Condition condition,
                final Map<String, Object> additionalRenderContext, String width, String height,
                final boolean uniqueNamespace)
        {
            this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
            this.iFrameRenderContextBuilderFactory = iFrameRenderContextBuilderFactory;
            this.templateRenderer = templateRenderer;
            this.addOnKey = addOnKey;
            this.moduleKey = moduleKey;
            this.template = template;
            this.accessDeniedTemplate = accessDeniedTemplate;
            this.urlTemplate = urlTemplate;
            this.title = title;
            this.decorator = decorator;
            this.condition = condition;
            this.additionalRenderContext = additionalRenderContext;
            this.width = width;
            this.height = height;
            this.uniqueNamespace = uniqueNamespace;
        }

        @Override
        public void render(final ModuleContextParameters moduleContextParameters, final Writer writer, Option<ModuleViewParameters> moduleViewParameterOverrides)
                throws IOException
        {
            String namespace = uniqueNamespace ? ModuleKeyGenerator.randomName(moduleKey) : moduleKey;

            String signedUri = iFrameUriBuilderFactory.builder()
                    .addOn(addOnKey)
                    .namespace(namespace)
                    .urlTemplate(urlTemplate)
                    .context(moduleContextParameters)
                    .signAndBuild();

            ModuleViewParameters moduleViewParameters = getModuleViewParameters(moduleViewParameterOverrides);

            Map<String, Object> renderContext = iFrameRenderContextBuilderFactory.builder()
                    .addOn(addOnKey)
                    .namespace(namespace)
                    .iframeUri(signedUri)
                    .decorator(decorator)
                    .title(title)
                    .context(additionalRenderContext)
                    .context("contextParams", moduleContextParameters)
                    .context("width", moduleViewParameters.getWidth())
                    .context("height", moduleViewParameters.getHeight())
                    .build();

            templateRenderer.render(template, renderContext, writer);
        }

        private ModuleViewParameters getModuleViewParameters(Option<ModuleViewParameters> moduleViewParameterOverrides)
        {
            // use override values if provided, falling back to defaults in module

            final String w = moduleViewParameterOverrides.flatMap(new Function<ModuleViewParameters, Option<String>>()
            {
                @Override
                public Option<String> apply(@Nullable ModuleViewParameters input)
                {
                    return Option.option(input.getWidth());
                }
            }).getOrElse(width);

            final String h = moduleViewParameterOverrides.flatMap(new Function<ModuleViewParameters, Option<String>>()
            {
                @Override
                public Option<String> apply(@Nullable ModuleViewParameters input)
                {
                    return Option.option(input.getHeight());
                }
            }).getOrElse(height);

            return new ModuleViewParameters(w, h);
        }


        @Override
        public void renderAccessDenied(final Writer writer) throws IOException
        {
            Map<String, Object> renderContext = ImmutableMap.<String, Object>builder()
                    .put("title", title)
                    .put("decorator", decorator)
                    .build();

            templateRenderer.render(accessDeniedTemplate, renderContext, writer);
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
