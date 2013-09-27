package com.atlassian.plugin.connect.plugin.module.context;

import com.atlassian.plugin.connect.plugin.module.common.user.CommonUserLookup;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.google.common.base.Optional;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class for parameter serializers / deserializers
 * @param <T> the type of the parameter (resource)
 * @param <C> the wrapped type for parameter lookups. Note Jira and Concluence tend to return result objects that contain
 *           the actual resource or an error
 * @param <U> The user type
 */
public abstract class AbstractParameterSerializer<T, C, U> implements ParameterSerializer<T>, ParameterDeserializer<T>
{
    /**
     * An interface for objects that look up the resource (wrapper) via a particular parameter (e.g. id, key, name etc)
     * TODO: Parameter name is likely confusing here. Maybe key? field? property?
     * @param <C> The (possibly) wrapped resource
     * @param <P> The type of the parameter that is being used as the key (e.g. String, Long etc)
     * @param <U> The type of the user
     */
    public static interface ParameterLookup<C, P, U>
    {
        /**
         * The name of the key to look up by
         */
        String getParamName();

        /**
         * The type of the lookup key
=        */
        Class<P> getType();

        /**
         * Looks up the resource by user and key value
         * @param user the user
         * @param value the value of the key
         * @return
         */
        C lookup(U user, P value);
    }

    /**
     * Interface for objects that unwrap a wrapped resource
     * @param <C> The type of the wrapper
     * @param <T> The type of the resource
     */
    public static interface ParameterUnwrapper<C, T>
    {
        T unwrap(C wrapped);
    }


    public static final String ID_FIELD_NAME = "id";
    public static final String KEY_FIELD_NAME = "key";
    private final CommonUserLookup<U> userManager;
    private final String containerFieldName;
    private final ParameterLookup<C, ?, U>[] parameterLookups;
    private final ParameterUnwrapper<C, T> parameterUnwrapper;

    /**
     * Creates an AbstractParameterSerializer with the userManager to lookup users, the field name for the top level map entry,
     * an object to unwrap looked up resources, and one or more objects to look up resources via different keys
     * @param userManager
     * @param containerFieldName
     * @param parameterUnwrapper
     * @param parameterLookups
     */
    public AbstractParameterSerializer(CommonUserLookup<U> userManager, String containerFieldName,
                                       ParameterUnwrapper<C, T> parameterUnwrapper,
                                       ParameterLookup<C, ?, U>... parameterLookups)
    {
        this.parameterUnwrapper = parameterUnwrapper;
        this.userManager = checkNotNull(userManager, "userManager is mandatory");
        this.containerFieldName = checkNotNull(containerFieldName);
        this.parameterLookups = parameterLookups;
    }

    @Override
    public Optional<T> deserialize(Map<String, Object> params, String username) throws ResourceNotFoundException,
            MalformedRequestException, UnauthorisedException
    {
        final Optional<Map> containerMap = getContainerMap(params);
        if (!containerMap.isPresent())
        {
            return Optional.absent();
        }

        final Optional<EncapsulatedLookupProxy> lookup = createLookup(containerMap);
        if (!lookup.isPresent())
        {
            throw new MalformedRequestException("No identifiers in request for " + containerFieldName);
        }

        final U user = userManager.lookupByUsername(username);
        // leave it to the applications to deal with a potentially null user as it likely represents "Guest"

        final C serviceResult = lookup.get().lookup(user);

        if (serviceResult == null || !isResultValid(serviceResult))
        {
            throwResourceNotFoundException();
        }

        final T resource = parameterUnwrapper.unwrap(serviceResult);
        checkViewPermission(resource, user);
        return Optional.of(resource);
    }

    protected void throwResourceNotFoundException() throws ResourceNotFoundException
    {
        throw new ResourceNotFoundException("No such " + containerFieldName);
    }

    protected void checkViewPermission(T resource, U user) throws UnauthorisedException, ResourceNotFoundException
    {
    }

    private Optional<Map> getContainerMap(Map<String, Object> params) throws MalformedRequestException
    {
        return getParam(params, containerFieldName, Map.class);
    }

    protected abstract boolean isResultValid(C serviceResult);

    protected U getUser(String username)
    {
        return userManager.lookupByUsername(username);
    }

    protected static <T> ParameterUnwrapper<T, T> createNoopUnwrapper(Class<T> projectComponentClass)
    {
        return new ParameterUnwrapper<T, T>()
        {
            @Override
            public T unwrap(T notReallyWrapped)
            {
                return notReallyWrapped;
            }
        };

    }

    protected abstract static class AbstractParameterLookup<C, P, U> implements ParameterLookup<C, P, U>
    {
        private final String paramName;
        private final Class<P> type;

        public AbstractParameterLookup(String paramName, Class<P> type)
        {
            this.paramName = paramName;
            this.type = type;
        }

        @Override
        public String getParamName()
        {
            return paramName;
        }

        @Override
        public Class<P> getType()
        {
            return type;
        }
    }

    protected abstract static class AbstractStringParameterLookup<C, U> extends AbstractParameterLookup<C, String, U>
    {
        public AbstractStringParameterLookup(String paramName)
        {
            super(paramName, String.class);
        }
    }

    protected abstract static class AbstractKeyParameterLookup<C, U> extends AbstractStringParameterLookup<C, U>
    {
        public AbstractKeyParameterLookup()
        {
            super(KEY_FIELD_NAME);
        }
    }

    protected abstract static class AbstractLongParameterLookup<C, U> extends AbstractParameterLookup<C, Long, U>
    {
        public AbstractLongParameterLookup(String paramName)
        {
            super(paramName, Long.class);
        }
    }

    protected abstract static class AbstractIdParameterLookup<C, U> extends AbstractLongParameterLookup<C, U>
    {
        public AbstractIdParameterLookup()
        {
            super(ID_FIELD_NAME);
        }
    }

    private <C> Optional<C> getParam(Map<?, ?> params, String paramName, Class<C> type) throws MalformedRequestException
    {
        final Object o = params.get(paramName);
        if (o == null)
        {
            return Optional.absent();
        }
        else if (!type.isInstance(o))
        {
            throw new MalformedRequestException("Invalid type for parameter name " + paramName);
        }
        else
        {
            return Optional.of((C) o);
        }
    }

    /**
     * Wraps a ParameterLookup such that it encapsulates the retrieval of the look up of the key value in the container map
     */
    private static interface EncapsulatedLookup<C, U>
    {
        C lookup(U user);
    }


    /*
     * Creates a EncapsulatedLookup by going through each of the provided ParameterLookups and checking for the presence of the
     * associated lookup key in the container map. The first ParameterLookup with a key present will be used to do
     * the actual lookup
     */
    private Optional<EncapsulatedLookupProxy> createLookup(Optional<Map> containerMap) throws MalformedRequestException
    {
        Optional<EncapsulatedLookupProxy> result = Optional.absent();
        if (containerMap.isPresent())
        {
            for (ParameterLookup<C, ?, U> parameterLookup : parameterLookups)
            {
                result = result.or(optionalLookup(createLookupFor(containerMap.get(), parameterLookup)));
            }
        }
        return result;
    }

    private <P> Optional<? extends EncapsulatedLookup> createLookupFor(Map<?, ?> params, final ParameterLookup<C, P, U> parameterLookup) throws MalformedRequestException
    {
        final String paramName = parameterLookup.getParamName();
        final Class<P> type = parameterLookup.getType();
        return Number.class.isAssignableFrom(type) ? createLookupForLong(params, paramName,
                (ParameterLookup<C, Long, U>) parameterLookup)
                : createLookupForNonLong(params, paramName, parameterLookup, type);
    }

    private <P> Optional<? extends EncapsulatedLookup> createLookupForNonLong(Map<?, ?> params, String paramName,
                                                                  final ParameterLookup<C, P, U> parameterLookup, final Class<P> cls) throws MalformedRequestException
    {
        final Optional<P> value = getParam(params, paramName, cls);
        return value.isPresent() ? Optional.of(new EncapsulatedLookup<C, U>()
        {
            @Override
            public C lookup(U user)
            {
                return parameterLookup.lookup(user, value.get());
            }
        }) : Optional.<EncapsulatedLookup>absent();
    }

    // TODO: Can we avoid the code dupe here? The difference is the need to type convert into Long. Could build type
    // optional conversion into the main lookup
    private Optional<? extends EncapsulatedLookup> createLookupForLong(Map<?, ?> params, String paramName,
                                                           final ParameterLookup<C, Long, U> parameterLookup) throws MalformedRequestException
    {
        final Optional<Object> optValue = getParam(params, paramName, Object.class);
        if (!optValue.isPresent())
        {
            return Optional.absent();
        }
        Object value = optValue.get();
        Long longValue = null;

        if (value instanceof Number)
        {
            longValue = ((Number) value).longValue();
        }
        else
        {
            try
            {
                longValue = Long.parseLong(value.toString());
            }
            catch (NumberFormatException e)
            {
                throw new MalformedRequestException("parameter " + paramName + " must be a number");
            }
        }
        final Long finalLong = longValue;

        return optValue.isPresent() ? Optional.of(new EncapsulatedLookup<C, U>()
        {
            @Override
            public C lookup(U user)
            {
                return parameterLookup.lookup(user, finalLong);
            }
        }) : Optional.<EncapsulatedLookup>absent();
    }


    // This wrapper class unfortunately needed to get around generics issue. At least I can't get it to work otherwise
    // Otherwise you end up with Optional<? extends EncapsulatedLookup> and calling or on that won't compile
    private class EncapsulatedLookupProxy implements EncapsulatedLookup<C, U>
    {
        private final EncapsulatedLookup<C, U> target;

        EncapsulatedLookupProxy(EncapsulatedLookup<C, U> target)
        {
            this.target = target;
        }

        @Override
        public C lookup(U user)
        {
            return target.lookup(user);
        }

        public Optional<EncapsulatedLookupProxy> option()
        {
            return Optional.of(this);
        }

    }

    private Optional<EncapsulatedLookupProxy> optionalLookup(Optional<? extends EncapsulatedLookup> target)
    {
        return target.isPresent() ? new EncapsulatedLookupProxy(target.get()).option() : Optional.<EncapsulatedLookupProxy>absent();
    }

}
