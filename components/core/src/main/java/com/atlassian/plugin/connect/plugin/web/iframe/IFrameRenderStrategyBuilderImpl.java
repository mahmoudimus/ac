package com.atlassian.plugin.connect.plugin.web.iframe;

import com.atlassian.plugin.connect.api.web.ModuleTemplate;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.ConnectUriFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilder;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.PermissionDeniedException;
import com.atlassian.plugin.connect.plugin.web.condition.ConnectConditionFactory;
import com.atlassian.plugin.web.Condition;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public class IFrameRenderStrategyBuilderImpl implements IFrameRenderStrategyBuilder,
        IFrameRenderStrategyBuilder.AddonUriBuilder, IFrameRenderStrategyBuilder.ModuleUriBuilder,
        IFrameRenderStrategyBuilder.TemplatedBuilder, IFrameRenderStrategyBuilder.InitializedBuilder {
    private static final String TEMPLATE_PATH = "velocity/";
    private static final String TEMPLATE_GENERIC_BODY = TEMPLATE_PATH + "iframe-body.vm";
    private static final String TEMPLATE_GENERIC_INLINE = TEMPLATE_PATH + "iframe-body-inline.vm";
    private static final String TEMPLATE_PAGE = TEMPLATE_PATH + "iframe-page.vm";
    private static final String TEMPLATE_JSON = TEMPLATE_PATH + "iframe-json.vm";

    private static final String TEMPLATE_ACCESS_DENIED_PAGE = TEMPLATE_PATH + "iframe-page-accessdenied.vm";
    private static final String TEMPLATE_ACCESS_DENIED_GENERIC_BODY = TEMPLATE_PATH + "iframe-body-accessdenied.vm";
    private static final String TEMPLATE_ACCESS_DENIED_JSON = TEMPLATE_PATH + "iframe-json-accessdenied.vm";

    public static final String ATL_GENERAL = "atl.general";

    private final ConnectUriFactory connectUriFactory;
    private final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory;
    private final TemplateRenderer templateRenderer;
    private final ConnectConditionFactory connectConditionFactory;

    private final Map<String, Object> additionalRenderContext = Maps.newHashMap();

    private String addonKey;
    private String moduleKey;
    private String template;
    private String accessDeniedTemplate;
    private String urlTemplate;
    private String title;
    private String decorator;
    private String width;
    private String height;
    private String contentType;
    private boolean uniqueNamespace;
    private boolean isDialog;
    private boolean isSimpleDialog;
    private boolean resizeToParent;
    private boolean sign = true; // should this url be signed?
    private boolean redirect = false;

    private final List<ConditionalBean> conditionalBeans = Lists.newArrayList();
    private final List<Class<? extends Condition>> conditionClasses = Lists.newArrayList();

    public IFrameRenderStrategyBuilderImpl(
            final ConnectUriFactory connectUriFactory,
            final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory,
            final TemplateRenderer templateRenderer,
            final ConnectConditionFactory connectConditionFactory) {
        this.connectUriFactory = connectUriFactory;
        this.iFrameRenderContextBuilderFactory = iFrameRenderContextBuilderFactory;
        this.templateRenderer = templateRenderer;
        this.connectConditionFactory = connectConditionFactory;
        this.contentType = ContentType.TEXT_HTML.getMimeType();
    }

    @Override
    public AddonUriBuilder addon(final String key) {
        addonKey = checkNotNull(key);
        return this;
    }

    @Override
    public ModuleUriBuilder module(final String key) {
        moduleKey = checkNotNull(key);
        return this;
    }

    @Override
    public TemplatedBuilder pageTemplate() {
        template = TEMPLATE_PAGE;
        accessDeniedTemplate = TEMPLATE_ACCESS_DENIED_PAGE;
        return this;
    }

    @Override
    public TemplatedBuilder genericBodyTemplate() {
        template = TEMPLATE_GENERIC_BODY;
        accessDeniedTemplate = TEMPLATE_ACCESS_DENIED_GENERIC_BODY;
        return this;
    }

    @Override
    public TemplatedBuilder genericBodyTemplate(boolean inline) {
        template = inline ? TEMPLATE_GENERIC_INLINE : TEMPLATE_GENERIC_BODY;
        accessDeniedTemplate = TEMPLATE_ACCESS_DENIED_GENERIC_BODY;
        return this;
    }

    @Override
    public TemplatedBuilder dialogTemplate() {
        template = TEMPLATE_GENERIC_BODY;
        accessDeniedTemplate = TEMPLATE_ACCESS_DENIED_GENERIC_BODY;
        return this;
    }

    @Override
    public TemplatedBuilder template(final ModuleTemplate moduleTemplate) {
        template = moduleTemplate.template;
        accessDeniedTemplate = moduleTemplate.accessDeniedTemplate;
        return this;
    }

    @Override
    public InitializedBuilder urlTemplate(final String urlTemplate) {
        this.urlTemplate = urlTemplate;
        return this;
    }

    @Override
    public InitializedBuilder condition(final ConditionalBean condition) {
        if (condition != null) {
            conditionalBeans.add(condition);
        }
        return this;
    }

    @Override
    public InitializedBuilder conditions(final Iterable<ConditionalBean> conditions) {
        Iterables.addAll(conditionalBeans, conditions);
        return this;
    }

    @Override
    public InitializedBuilder conditionClass(final Class<? extends Condition> condition) {
        if (condition != null) {
            conditionClasses.add(condition);
        }
        return this;
    }

    @Override
    public InitializedBuilder conditionClasses(final Iterable<Class<? extends Condition>> conditions) {
        Iterables.addAll(conditionClasses, conditions);
        return this;
    }

    @Override
    public InitializedBuilder title(final String title) {
        this.title = title;
        return this;
    }

    @Override
    public InitializedBuilder decorator(final String decorator) {
        this.decorator = decorator;
        return this;
    }

    @Override
    public InitializedBuilder dimensions(String width, String height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public InitializedBuilder additionalRenderContext(final String key, final Object object) {
        this.additionalRenderContext.put(key, object);
        return this;
    }

    @Override
    public InitializedBuilder ensureUniqueNamespace(boolean uniqueNamespace) {
        this.uniqueNamespace = uniqueNamespace;
        return this;
    }

    @Override
    public InitializedBuilder dialog(boolean isDialog) {
        this.isDialog = isDialog;
        return this;
    }

    @Override
    public InitializedBuilder simpleDialog(boolean isSimpleDialog) {
        if (isSimpleDialog) {
            this.isDialog = true;
        }
        this.isSimpleDialog = isSimpleDialog;
        return this;
    }

    @Override
    public InitializedBuilder resizeToParent(boolean resizeToParent) {
        this.resizeToParent = resizeToParent;
        return this;
    }

    @Override
    public InitializedBuilder sign(final boolean sign) {
        this.sign = sign;
        return this;
    }

    @Override
    public InitializedBuilder redirect(final boolean redirect) {
        this.redirect = redirect;
        return this;
    }

    @Override
    public IFrameRenderStrategy build() {
        Condition condition = connectConditionFactory.createCondition(addonKey, conditionalBeans, conditionClasses);

        return new IFrameRenderStrategyImpl(connectUriFactory, iFrameRenderContextBuilderFactory, templateRenderer,
                addonKey, moduleKey, template, accessDeniedTemplate, urlTemplate, title,
                decorator, condition, additionalRenderContext, width, height, uniqueNamespace, isDialog, isSimpleDialog,
                resizeToParent, sign, contentType, redirect);
    }

    @VisibleForTesting
    public static class IFrameRenderStrategyImpl implements IFrameRenderStrategy {

        private final ConnectUriFactory connectUriFactory;
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
        private final String contentType;
        private final boolean uniqueNamespace;
        private final boolean isDialog;
        private final boolean isSimpleDialog;
        private final String decorator;
        private final Condition condition;
        private final boolean resizeToParent;
        private final boolean sign;
        private final boolean redirect;

        private IFrameRenderStrategyImpl(
                final ConnectUriFactory connectUriFactory,
                final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory,
                final TemplateRenderer templateRenderer,
                final String addOnKey,
                final String moduleKey,
                final String template,
                final String accessDeniedTemplate,
                final String urlTemplate,
                final String title,
                final String decorator,
                final Condition condition,
                final Map<String, Object> additionalRenderContext,
                final String width,
                final String height,
                final boolean uniqueNamespace,
                final boolean isDialog,
                final boolean isSimpleDialog,
                final boolean resizeToParent,
                final boolean sign,
                final String contentType,
                boolean redirect) {
            this.connectUriFactory = connectUriFactory;
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
            this.isDialog = isDialog;
            this.isSimpleDialog = isDialog;
            this.resizeToParent = resizeToParent;
            this.sign = sign;
            this.contentType = contentType;
            this.redirect = redirect;
        }

        @Override
        public void render(final ModuleContextParameters moduleContextParameters, final Writer writer, Optional<String> uiParameters)
                throws IOException {
            String namespace = generateNamespace();

            String signedUri = buildUrl(moduleContextParameters, uiParameters, namespace);

            Map<String, Object> renderContext = iFrameRenderContextBuilderFactory.builder()
                    .addon(addOnKey)
                    .namespace(namespace)
                    .iframeUri(signedUri)
                    .decorator(decorator)
                    .title(title)
                    .dialog(isDialog)
                    .simpleDialog(isSimpleDialog)
                    .resizeToParent(resizeToParent)
                    .context(additionalRenderContext)
                    .context("contextParams", moduleContextParameters)
                    .productContext(moduleContextParameters)
                    .context("width", width)
                    .context("height", height)
                    .build();

            templateRenderer.render(template, renderContext, writer);
        }

        private String generateNamespace() {
            return uniqueNamespace ? ModuleKeyUtils.randomName(moduleKey) : moduleKey;
        }

        @VisibleForTesting
        public String buildUrl(ModuleContextParameters moduleContextParameters, Optional<String> uiParameters) {
            return buildUrl(moduleContextParameters, uiParameters, generateNamespace());
        }

        private String buildUrl(ModuleContextParameters moduleContextParameters, Optional<String> uiParameters, String namespace) {
            if (redirect) {
                return connectUriFactory.createRedirectServletUri(addOnKey, namespace, moduleContextParameters);
            }
            return connectUriFactory.createConnectAddonUriBuilder()
                    .addon(addOnKey)
                    .namespace(namespace)
                    .urlTemplate(urlTemplate)
                    .context(moduleContextParameters)
                    .uiParams(uiParameters)
                    .dialog(isDialog)
                    .sign(sign)
                    .build();
        }

        @Override
        public void renderAccessDenied(final Writer writer) throws IOException {
            Map<String, Object> renderContext = ImmutableMap.<String, Object>builder()
                    .put("title", StringUtils.defaultIfEmpty(title, ""))
                    .put("decorator", ATL_GENERAL)
                    .build();

            templateRenderer.render(accessDeniedTemplate, renderContext, writer);
        }

        @Override
        public boolean shouldShow(Map<String, ? extends Object> conditionContext) {
            return condition == null || condition.shouldDisplay(new HashMap<>(conditionContext));
        }

        @Override
        public boolean needRedirection() {
            return redirect;
        }

        @Override
        public void shouldShowOrThrow(final Map<String, Object> conditionContext) {
            if (!shouldShow(conditionContext)) {
                throw new PermissionDeniedException(addOnKey, "Cannot render iframe for this page.");
            }
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public IFrameRenderStrategy toJsonRenderStrategy() {
            return new IFrameRenderStrategyImpl(connectUriFactory, iFrameRenderContextBuilderFactory, templateRenderer,
                    addOnKey, moduleKey, TEMPLATE_JSON, TEMPLATE_ACCESS_DENIED_JSON, urlTemplate, title,
                    decorator, condition, additionalRenderContext, width, height, uniqueNamespace, isDialog, isSimpleDialog,
                    resizeToParent, sign, ContentType.APPLICATION_JSON.getMimeType(), redirect);
        }
    }
}