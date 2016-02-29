package com.atlassian.plugin.connect.jira.report;

import com.atlassian.fugue.Option;
import com.atlassian.jira.plugin.report.Report;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.plugin.report.ReportModuleDescriptorImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.lifecycle.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.api.web.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.web.context.JiraModuleContextParameters;
import com.atlassian.plugin.connect.jira.web.context.JiraModuleContextParametersImpl;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import com.google.common.base.CharMatcher;
import org.dom4j.Element;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyMap;

/**
 * Connect version of JIRA report module.
 *
 * @since 1.2
 */
public class ConnectReportModuleDescriptor extends AbstractModuleDescriptor<Void> {
    public final static String THUMBNAIL_CSS_CLASS_PREFIX = "connect-report-thumbnail-";
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final JiraAuthenticationContext authContext;
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ModuleContextFilter moduleContextFilter;

    private DynamicDescriptorRegistration.Registration registration;
    private Element descriptor;
    private String thumbnailUrl;

    public ConnectReportModuleDescriptor(JiraAuthenticationContext authenticationContext,
                                         ModuleFactory moduleFactory,
                                         IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                         DynamicDescriptorRegistration dynamicDescriptorRegistration,
                                         UrlVariableSubstitutor urlVariableSubstitutor,
                                         ModuleContextFilter moduleContextFilter) {
        super(moduleFactory);
        this.authContext = authenticationContext;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public void init(@NotNull final Plugin plugin, @NotNull final Element element) throws PluginParseException {
        super.init(plugin, element);
        this.descriptor = element;
    }

    @Override
    public void enabled() {
        super.enabled();
        this.registration = dynamicDescriptorRegistration.registerDescriptors(getReportDescriptor());
    }

    @Override
    public void disabled() {
        if (registration != null) {
            registration.unregister();
        }
        super.disabled();
    }

    private ModuleDescriptor<Report> getReportDescriptor() {
        ReportModuleDescriptor moduleDescriptor = new ModuleDescriptorImpl(iFrameRenderStrategyRegistry, descriptor, urlVariableSubstitutor, moduleContextFilter, ConnectReportModuleDescriptor.this, thumbnailUrl);
        moduleDescriptor.init(plugin, descriptor);
        return moduleDescriptor;
    }

    @Override
    public Void getModule() {
        return null;
    }

    public void setThumbnailUrl(final String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public static String getThumbnailCssClass(String key) {
        return THUMBNAIL_CSS_CLASS_PREFIX + makeSafeCssClass(key);
    }

    private static String makeSafeCssClass(String string) {
        return CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.anyOf("_-")).retainFrom(string);
    }

    public static class ModuleDescriptorImpl extends ReportModuleDescriptorImpl {
        private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
        private final Element descriptor;
        private final UrlVariableSubstitutor urlVariableSubstitutor;
        private final ModuleContextFilter moduleContextFilter;
        private final String thumbnailUrl;


        public ModuleDescriptorImpl(final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry, final Element descriptor, final UrlVariableSubstitutor urlVariableSubstitutor, final ModuleContextFilter moduleContextFilter, final ConnectReportModuleDescriptor connectReportModuleDescriptor, final String thumbnailUrl) {
            super(connectReportModuleDescriptor.authContext, connectReportModuleDescriptor.moduleFactory);
            this.iFrameRenderStrategyRegistry = checkNotNull(iFrameRenderStrategyRegistry);
            this.descriptor = checkNotNull(descriptor);
            this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
            this.moduleContextFilter = checkNotNull(moduleContextFilter);
            this.thumbnailUrl = checkNotNull(thumbnailUrl);
        }

        @Override
        public Report getModule() {
            return new ConnectReport(iFrameRenderStrategyRegistry, getPluginKey(), getKey());
        }

        @Override
        public String getUrl(final Project project) {
            final String url = descriptor.attribute("url") != null ? descriptor.attribute("url").getValue() : "";
            final JiraModuleContextParameters unfilteredContext = new JiraModuleContextParametersImpl(emptyMap());
            unfilteredContext.addProject(project);

            return urlVariableSubstitutor.append(url, moduleContextFilter.filter(unfilteredContext));
        }

        @Override
        public Option<String> getUrl(final Map<String, Object> context) {
            final Object projectFromCtx = context.get("project");
            if (projectFromCtx != null) {
                return Option.some(getUrl((Project) projectFromCtx));
            } else {
                return Option.none();
            }
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }
    }
}
