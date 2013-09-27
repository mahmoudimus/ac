package com.atlassian.plugin.connect.plugin.module.context;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Base class for ContextMapParameterExtractors
 *
 * @param P the parameter (resource) type
 */
public abstract class AbstractContextMapParameterExtractor<P> implements ContextMapParameterExtractor<P>
{
    private final Class<P> resourceClass;
    private final ParameterSerializer<P> parameterSerializer;
    private final ParameterDeserializer<P> parameterDeserializer;
    private final String contextParameterKey;
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContextMapParameterExtractor.class);

    public AbstractContextMapParameterExtractor(Class<P> resourceClass,
                                                ParameterSerializer<P> parameterSerializer,
                                                ParameterDeserializer<P> parameterDeserializer,
                                                String contextParameterKey)
    {
        this.resourceClass = resourceClass;
        this.parameterSerializer = parameterSerializer;
        this.parameterDeserializer = parameterDeserializer;
        this.contextParameterKey = contextParameterKey;
    }

    public AbstractContextMapParameterExtractor(Class<P> resourceClass,
                                                CombinedParameterSerializer<P> combinedParameterSerializer,
                                                String contextParameterKey)
    {
        this(resourceClass, combinedParameterSerializer, combinedParameterSerializer, contextParameterKey);
    }

    @Override
    public Optional<P> extract(final Map<String, Object> context)
    {
        if (context.containsKey(contextParameterKey) && resourceClass.isInstance(context.get(contextParameterKey)))
        {
            return Optional.fromNullable(getResource(context));
        }
        return Optional.absent();
    }

    protected P getResource(Map<String, Object> context)
    {
        return (P) context.get(contextParameterKey);
    }

    @Override
    public ParameterSerializer<P> serializer()
    {
        return parameterSerializer;
    }

    @Override
    public ParameterDeserializer<P> deserializer()
    {
        return parameterDeserializer;
    }
}
