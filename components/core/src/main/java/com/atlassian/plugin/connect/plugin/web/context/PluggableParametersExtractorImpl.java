package com.atlassian.plugin.connect.plugin.web.context;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterResolverModuleDescriptor;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterResolverModuleDescriptor.ConnectContextParametersResolver;
import com.atlassian.plugin.connect.spi.web.context.HashMapModuleContextParameters;
import com.atlassian.plugin.connect.spi.web.context.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This component extracts context parameters based on local Connect project extractors as well as extractors loaded from plug-ins.
 */
@Component
@ExportAsService
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

    public Map<String, String> extractParameters(Map<String, Object> context)
    {
        Map<String, String> moduleContextParameters = new HashMapModuleContextParameters();
        moduleContextParameters.putAll(connectModuleContextExtractor.extractParameters(context));
        moduleContextParameters.putAll(extractFromPluginProvidedExtractors(context));
        moduleContextParameters.putAll(extractFromPluginProvidedMappers(context));
        return moduleContextParameters;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractFromPluginProvidedMappers(final Map<String, Object> context)
    {
        ImmutableMap.Builder<String, String> contextParameters = ImmutableMap.builder();
        List<ConnectContextParameterMapper> parameterMappers = pluginAccessor.getEnabledModulesByClass(ConnectContextParameterMapper.class);
        for (ConnectContextParameterMapper parameterMapper : parameterMappers)
        {
            parameterMapper.extractContextValue(context).ifPresent(new Consumer()
            {

                @Override
                public void accept(Object contextValue)
                {
                    if (parameterMapper.isParameterValueAccessibleByCurrentUser(contextValue))
                    {
                        contextParameters.put(parameterMapper.getParameterKey(), parameterMapper.getParameterValue(contextValue));
                    }
                }
            });
        }
        return contextParameters.build();
    }

    private Map<String, String> extractFromPluginProvidedExtractors(final Map<String, Object> context)
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
