package com.atlassian.plugin.connect.plugin.web.context;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper.Parameter;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterResolverModuleDescriptor;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterResolverModuleDescriptor.ConnectContextParametersResolver;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This component extracts context parameters based on local Connect project extractors as well as extractors loaded from plug-ins.
 */
@Component
@ExportAsService
public class PluggableParametersExtractorImpl implements PluggableParametersExtractor
{
    private final static Logger log = LoggerFactory.getLogger(PluggableParametersExtractorImpl.class);

    private final PluginAccessor pluginAccessor;
    private PluggableContextParameterValidator pluggableContextParameterValidator;

    @Autowired
    public PluggableParametersExtractorImpl(PluginAccessor pluginAccessor,
            PluggableContextParameterValidator pluggableContextParameterValidator)
    {
        this.pluginAccessor = pluginAccessor;
        this.pluggableContextParameterValidator = pluggableContextParameterValidator;
    }

    public Map<String, String> extractParameters(Map<String, Object> context)
    {
        Map<String, String> moduleContextParameters = new HashMap<>();
        moduleContextParameters.putAll(extractFromPluginProvidedExtractors(context));
        moduleContextParameters.putAll(extractFromPluginProvidedMappers(context));
        return moduleContextParameters;
    }

    @Override
    public Map<String, String> getParametersAccessibleByCurrentUser(Map<String, String> contextParameters)
    {
        List<ConnectContextParameterMapper> contextParameterMappers = getContextParameterMappers();

        Map<String, Parameter> supportedParameters = getParametersFromMappers(contextParameterMappers);
        Map<String, String> accessibleContextParameters = contextParameters.entrySet().stream()
                .filter((contextParameter) -> supportedParameters.containsKey(contextParameter.getKey()))
                .filter((contextParameter) -> {
                    Parameter parameter = supportedParameters.get(contextParameter.getKey());
                    return parameter.isAccessibleByCurrentUser(contextParameter.getValue());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        accessibleContextParameters.putAll(filterByPluginProvidedValidators(contextParameters, supportedParameters));

        return accessibleContextParameters;
    }

    private List<ConnectContextParameterMapper> getContextParameterMappers()
    {
        return pluginAccessor.getEnabledModulesByClass(ConnectContextParameterMapper.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractFromPluginProvidedMappers(final Map<String, Object> context)
    {
        List<ConnectContextParameterMapper> parameterMappers = getContextParameterMappers();

        List<TypeBasedConnectContextParameterMapper> typeBasedParameterMappers = getTypeBasedParameterMappers(parameterMappers);
        Map<String, String> contextParameters = extractFromTypeBasedParameterMappers(context, typeBasedParameterMappers);

        List<ConnectContextParameterMapper> customParameterMappers = new ArrayList<>(parameterMappers);
        customParameterMappers.removeAll(typeBasedParameterMappers);
        Map<String, String> customContextParameters = extractFromCustomParameterMappers(context, customParameterMappers);
        contextParameters.putAll(customContextParameters);

        return contextParameters;
    }

    private List<TypeBasedConnectContextParameterMapper> getTypeBasedParameterMappers(List<ConnectContextParameterMapper> parameterMappers)
    {
        return parameterMappers.stream()
                .filter(TypeBasedConnectContextParameterMapper.class::isInstance)
                .map(TypeBasedConnectContextParameterMapper.class::cast)
                .collect(Collectors.toList());
    }

    private Map<String, String> extractFromTypeBasedParameterMappers(Map<String, Object> context, List<TypeBasedConnectContextParameterMapper> typeBasedParameterMappers)
    {
        Map<Class, Supplier> contextValueSuppliers = getContextValueSuppliers(context, typeBasedParameterMappers);
        return buildContext(typeBasedParameterMappers, contextValueSuppliers);
    }

    private Map<Class, Supplier> getContextValueSuppliers(Map<String, Object> context,
            List<TypeBasedConnectContextParameterMapper> parameterMappers)
    {
        Map<Class, Supplier> contextValueSuppliers = new HashMap<>();
        for (TypeBasedConnectContextParameterMapper parameterMapper : parameterMappers)
        {
            parameterMapper.extractContextValue(context).ifPresent((contextValue) ->
            {
                Class contextValueClass = parameterMapper.getContextValueClass();
                contextValueSuppliers.put(contextValueClass, () -> contextValue);
                Set<TypeBasedConnectContextParameterMapper.AdditionalValue> additionalValues = parameterMapper.getAdditionalContextValues();
                for (TypeBasedConnectContextParameterMapper.AdditionalValue additionalValue : additionalValues)
                {
                    contextValueSuppliers.putIfAbsent(additionalValue.getContextValueClass(), () -> additionalValue.getValue(contextValue));
                }
            });
        }
        return contextValueSuppliers;
    }

    private Map<String, String> buildContext(List<TypeBasedConnectContextParameterMapper> parameterMappers,
            Map<Class, Supplier> contextValueSuppliers)
    {
        Map<String, String> contextParameters = new HashMap<>();
        for (TypeBasedConnectContextParameterMapper parameterMapper : parameterMappers)
        {
            Supplier contextValueSupplier = contextValueSuppliers.get(parameterMapper.getContextValueClass());
            if (contextValueSupplier != null)
            {
                contextParameters.putAll(serializeParameters(contextValueSupplier.get(), parameterMapper.getParameters()));
            }
        }
        return contextParameters;
    }

    private Map<String, String> extractFromCustomParameterMappers(Map<String, Object> context,
            List<ConnectContextParameterMapper> parameterMappers)
    {
        Map<String, String> contextParameters = new HashMap<>();
        for (ConnectContextParameterMapper parameterMapper : parameterMappers)
        {
            parameterMapper.extractContextValue(context).ifPresent((contextValue) -> {
                contextParameters.putAll(serializeParameters(contextValue, parameterMapper.getParameters()));
            });
        }
        return contextParameters;
    }

    private Map<String, String> serializeParameters(Object contextValue, Set<Parameter> parameters)
    {
        return parameters.stream().filter((parameter) -> parameter.isAccessibleByCurrentUser(contextValue))
                                .collect(Collectors.toMap(Parameter::getKey, (parameter) -> parameter.getValue(contextValue)));
    }

    private Map<String, Parameter> getParametersFromMappers(List<ConnectContextParameterMapper> contextParameterMappers)
    {
        return contextParameterMappers.stream()
                .flatMap((parameterMapper) -> ((Set<Parameter>) parameterMapper.getParameters()).stream())
                .collect(Collectors.toMap(Parameter::getKey, (parameter) -> parameter));
    }

    private Map<String, String> extractFromPluginProvidedExtractors(final Map<String, Object> context)
    {
        Map<String, String> contextParameters = new HashMap<>();
        for (ContextParametersExtractor contextParametersExtractor : getExtractors())
        {
            try
            {
                contextParameters.putAll(contextParametersExtractor.extractParameters(context));
            }
            catch (Throwable ex)
            {
                log.warn("Exception when extracting parameters", ex);
            }
        }

        return pluggableContextParameterValidator.filter(contextParameters);
    }

    private Iterable<ContextParametersExtractor> getExtractors()
    {
        return Iterables.concat(Iterables.transform(pluginAccessor.getModules(
                new ModuleDescriptorOfClassPredicate<>(ConnectContextParameterResolverModuleDescriptor.class))
                , new com.google.common.base.Function<ConnectContextParametersResolver, List<ContextParametersExtractor>>()
        {
            @Override
            public List<ContextParametersExtractor> apply(final ConnectContextParametersResolver input)
            {
                return input.getExtractors();
            }
        }));
    }

    private Map<String, String> filterByPluginProvidedValidators(Map<String, String> contextParameters, Map<String, Parameter> supportedParameters)
    {
        Map<String, String> unsupportedContextParameters = contextParameters.entrySet().stream()
                .filter((contextParameter) -> !supportedParameters.containsKey(contextParameter.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return pluggableContextParameterValidator.filter(unsupportedContextParameters);
    }
}
