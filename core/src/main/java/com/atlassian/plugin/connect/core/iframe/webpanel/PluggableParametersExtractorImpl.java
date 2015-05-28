package com.atlassian.plugin.connect.core.iframe.webpanel;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.iframe.webpanel.PluggableParametersExtractor;
import com.atlassian.plugin.connect.spi.iframe.context.HashMapModuleContextParameters;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.spi.iframe.context.module.ConnectContextParameterResolverModuleDescriptor;
import com.atlassian.plugin.connect.spi.iframe.context.module.ConnectContextParameterResolverModuleDescriptor.ConnectContextParametersResolver;
import com.atlassian.plugin.connect.spi.iframe.webpanel.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * This component extracts context parameters based on local Connect project extractors as well as extractors loaded from plug-ins.
 */
@Component
public class PluggableParametersExtractorImpl implements PluggableParametersExtractor
{
    private final static Logger log = LoggerFactory.getLogger(PluggableParametersExtractorImpl.class);

    private final WebFragmentModuleContextExtractor connectModuleContextExtractor;
    private final PluginAccessor pluginAccessor;

    @Autowired
    public PluggableParametersExtractorImpl(final WebFragmentModuleContextExtractor connectModuleContextExtractor, final PluginAccessor pluginAccessor)
    {
        this.connectModuleContextExtractor = connectModuleContextExtractor;
        this.pluginAccessor = pluginAccessor;
    }

    public ModuleContextParameters extractParameters(Map<String, Object> context)
    {
        ModuleContextParameters moduleContextParameters = new HashMapModuleContextParameters();
        moduleContextParameters.putAll(connectModuleContextExtractor.extractParameters(context));
        moduleContextParameters.putAll(extractByPlugins(context));
        return moduleContextParameters;
    }

    private Map<String, String> extractByPlugins(final Map<String, Object> context)
    {
        ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
        for (ContextParametersExtractor contextParametersExtractor : getExtractors())
        {
            try
            {
                result.putAll(contextParametersExtractor.extractParameters(context));
            }
            catch (Throwable ex)
            {
                log.warn("Exception when extracting parameters", ex);
            }
        }

        return result.build();
    }

    private Iterable<ContextParametersExtractor> getExtractors()
    {
        return Iterables.concat(Iterables.transform(pluginAccessor.getModules(
                new ModuleDescriptorOfClassPredicate<ConnectContextParametersResolver>(ConnectContextParameterResolverModuleDescriptor.class))
                , new Function<ConnectContextParametersResolver, List<ContextParametersExtractor>>()
        {
            @Override
            public List<ContextParametersExtractor> apply(final ConnectContextParametersResolver input)
            {
                return input.getExtractors();
            }
        }));
    }
}
