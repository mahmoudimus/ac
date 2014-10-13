package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.json.schema.util.StringUtil;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.report.ConnectReportModuleDescriptor;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.QueryParams;
import com.atlassian.plugin.webresource.transformer.CharSequenceDownloadableResource;
import com.atlassian.plugin.webresource.transformer.TransformableResource;
import com.atlassian.plugin.webresource.transformer.TransformerParameters;
import com.atlassian.plugin.webresource.transformer.TransformerUrlBuilder;
import com.atlassian.plugin.webresource.transformer.UrlReadingWebResourceTransformer;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformerFactory;
import com.atlassian.plugin.webresource.url.UrlBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class ReportModuleCssTransformer implements WebResourceTransformerFactory
{
    private final PluginAccessor pluginAccessor;

    public ReportModuleCssTransformer(final PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public TransformerUrlBuilder makeUrlBuilder(final TransformerParameters transformerParameters)
    {
        return new ReportModulesUriBuilder(pluginAccessor);
    }

    @Override
    public UrlReadingWebResourceTransformer makeResourceTransformer(final TransformerParameters transformerParameters)
    {
        return new UrlReadingWebResourceTransformer()
        {
            @Override
            public DownloadableResource transform(final TransformableResource transformableResource, final QueryParams queryParams)
            {
                return new ThumbnailCssClassesGenerator(transformableResource.nextResource(), pluginAccessor);
            }
        };
    }

    static class ReportModulesUriBuilder implements TransformerUrlBuilder
    {
        private final PluginAccessor pluginAccessor;

        ReportModulesUriBuilder(final PluginAccessor pluginAccessor) {this.pluginAccessor = pluginAccessor;}

        @Override
        public void addToUrl(final UrlBuilder urlBuilder)
        {
            final List<ConnectReportModuleDescriptor.ModuleDescriptorImpl> moduleDescriptorsByClass =
                    pluginAccessor.getEnabledModuleDescriptorsByClass(ConnectReportModuleDescriptor.ModuleDescriptorImpl.class);

            final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
            for (ConnectReportModuleDescriptor.ModuleDescriptorImpl moduleDescriptor : moduleDescriptorsByClass)
            {
                hashCodeBuilder.append(moduleDescriptor.getKey());
            }
            urlBuilder.addToHash("connect report thumbnail", hashCodeBuilder.build());
        }
    }

    static class ThumbnailCssClassesGenerator extends CharSequenceDownloadableResource
    {
        private final PluginAccessor pluginAccessor;

        protected ThumbnailCssClassesGenerator(final DownloadableResource originalResource, final PluginAccessor pluginAccessor)
        {
            super(originalResource);
            this.pluginAccessor = pluginAccessor;
        }

        @Override
        protected CharSequence transform(final CharSequence original)
        {
            final List<ConnectReportModuleDescriptor.ModuleDescriptorImpl> moduleDescriptorsByClass =
                    pluginAccessor.getEnabledModuleDescriptorsByClass(ConnectReportModuleDescriptor.ModuleDescriptorImpl.class);

            final StringBuilder stringBuilder = new StringBuilder();
            for (ConnectReportModuleDescriptor.ModuleDescriptorImpl moduleDescriptor : moduleDescriptorsByClass)
            {
                if (StringUtil.isNotBlank(moduleDescriptor.getThumbnailUrl()))
                {
                    stringBuilder
                            .append(".")
                            .append(ConnectReportModuleDescriptor.getThumbnailCssClass(moduleDescriptor.getKey()))
                            .append(":before ")
                            .append("{ background-image: url('")
                            .append(moduleDescriptor.getThumbnailUrl())
                            .append("') !important }\n");
                }
            }
            return stringBuilder.toString();
        }
    }
}
