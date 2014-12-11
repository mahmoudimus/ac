package com.atlassian.plugin.connect.plugin.iframe.webpanel;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.module.ConnectContextVariablesExtractorModuleDescriptor;
import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * This component extracts context parameters based on local Connect project extractors as well as extractors loaded from plug-ins.
 */
@Component
public class PluggableParametersExtractor
{
    private final WebFragmentModuleContextExtractor connectModuleContextExtractor;
    private final PluginAccessor pluginAccessor;

    @Autowired
    public PluggableParametersExtractor(final WebFragmentModuleContextExtractor connectModuleContextExtractor, final PluginAccessor pluginAccessor)
    {
        this.connectModuleContextExtractor = connectModuleContextExtractor;
        this.pluginAccessor = pluginAccessor;
    }

    public ModuleContextParameters extractParameters(Map<String, ? extends Object> context)
    {
        ModuleContextParameters moduleContextParameters = connectModuleContextExtractor.extractParameters(context);
        moduleContextParameters.putAll(extractByPlugins(context));
        return moduleContextParameters;
    }

    private Map<String, String> extractByPlugins(final Map<String, ? extends Object> context)
    {
        ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
        for (ContextParametersExtractor contextParametersExtractor : getExtractors())
        {
            result.putAll(contextParametersExtractor.extractParameters(context));
        }

        return result.build();
    }

    private Iterable<ContextParametersExtractor> getExtractors()
    {
        return pluginAccessor.getModules(new ModuleDescriptorOfClassPredicate<ContextParametersExtractor>(ConnectContextVariablesExtractorModuleDescriptor.class));
    }
}
