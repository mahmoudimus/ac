package com.atlassian.plugin.connect.jira.report;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.CharSequenceDownloadableResource;
import com.atlassian.plugin.webresource.transformer.TransformerParameters;
import com.atlassian.plugin.webresource.transformer.TransformerUrlBuilder;
import com.atlassian.plugin.webresource.transformer.UrlReadingWebResourceTransformer;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformerFactory;
import com.atlassian.plugin.webresource.url.UrlBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ReportModuleCssTransformer implements WebResourceTransformerFactory {

    private final PluginAccessor pluginAccessor;

    public ReportModuleCssTransformer(final PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public TransformerUrlBuilder makeUrlBuilder(final TransformerParameters transformerParameters) {
        return new ReportModulesUriBuilder(pluginAccessor);
    }

    @Override
    public UrlReadingWebResourceTransformer makeResourceTransformer(final TransformerParameters transformerParameters) {
        return (transformableResource, queryParams) -> new ThumbnailCssClassesGenerator(transformableResource.nextResource(), pluginAccessor);
    }

    static class ReportModulesUriBuilder implements TransformerUrlBuilder {
        private final PluginAccessor pluginAccessor;

        ReportModulesUriBuilder(final PluginAccessor pluginAccessor) {
            this.pluginAccessor = pluginAccessor;
        }

        @Override
        public void addToUrl(final UrlBuilder urlBuilder) {
            final List<ConnectReportModuleDescriptor.ModuleDescriptorImpl> moduleDescriptorsByClass =
                    pluginAccessor.getEnabledModuleDescriptorsByClass(ConnectReportModuleDescriptor.ModuleDescriptorImpl.class);

            final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
            for (ConnectReportModuleDescriptor.ModuleDescriptorImpl moduleDescriptor : moduleDescriptorsByClass) {
                hashCodeBuilder.append(moduleDescriptor.getKey());
            }
            urlBuilder.addToHash("connect report thumbnail", hashCodeBuilder.build());
        }
    }

    static class ThumbnailCssClassesGenerator extends CharSequenceDownloadableResource {
        private final PluginAccessor pluginAccessor;

        protected ThumbnailCssClassesGenerator(final DownloadableResource originalResource, final PluginAccessor pluginAccessor) {
            super(originalResource);
            this.pluginAccessor = pluginAccessor;
        }

        @Override
        protected CharSequence transform(final CharSequence original) {
            final List<ConnectReportModuleDescriptor.ModuleDescriptorImpl> moduleDescriptorsByClass =
                    pluginAccessor.getEnabledModuleDescriptorsByClass(ConnectReportModuleDescriptor.ModuleDescriptorImpl.class);

            final StringBuilder stringBuilder = new StringBuilder();
            moduleDescriptorsByClass.stream()
                    .filter(moduleDescriptor -> !isNullOrEmpty(moduleDescriptor.getThumbnailUrl()))
                    .forEach(moduleDescriptor -> stringBuilder
                            .append(".")
                            .append(ConnectReportModuleDescriptor.getThumbnailCssClass(moduleDescriptor.getKey()))
                            .append(":before ")
                            .append("{ background-image: url('")
                            .append(moduleDescriptor.getThumbnailUrl())
                            .append("') !important }\n")
                    );
            return stringBuilder.toString();
        }
    }
}
