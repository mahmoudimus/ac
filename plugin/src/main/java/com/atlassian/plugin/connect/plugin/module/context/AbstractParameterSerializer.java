package com.atlassian.plugin.connect.plugin.module.context;

import com.atlassian.plugin.connect.plugin.module.common.user.CommonUserLookup;
import com.google.common.base.Optional;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractParameterSerializer<T, C, U> implements ParameterSerializer<T>, ParameterDeserializer<T>
{
    public static final String ID_FIELD_NAME = "id";
    public static final String KEY_FIELD_NAME = "key";
    private final CommonUserLookup<U> userManager;
    private final String containerFieldName;
    private final ParameterLookup<C, ?, U>[] parameterLookups;
    private final ParameterUnwrapper<C, T> parameterUnwrapper;

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
    public Optional<T> deserialize(Map<String, Object> params, String username) throws ResourceNotFoundException, MalformedRequestException
    {
        final Optional<Map> containerMap = getContainerMap(params);
        if (!containerMap.isPresent())
        {
            return Optional.absent();
        }

        final Optional<LookupProxy> lookup = createLookup(containerMap);
        if (!lookup.isPresent())
        {
            throw new MalformedRequestException("No identifiers in request for " + containerFieldName);
        }

        final U user = userManager.lookupByUsername(username);
        if (user == null)
        {
            // TODO: Should this be an exception?
            return Optional.absent();
        }

        final C serviceResult = lookup.get().lookup(user);

        if (serviceResult == null || !isResultValid(serviceResult))
        {
            throw new ResourceNotFoundException("No such " + containerFieldName);
        }

        return Optional.of(parameterUnwrapper.unwrap(serviceResult));
    }

    private Optional<Map> getContainerMap(Map<String, Object> params) throws MalformedRequestException
    {
        return getParam(params, containerFieldName, Map.class);
    }

    protected abstract boolean isResultValid(C serviceResult);

    public static interface ParameterLookup<C, P, U>
    {
        String getParamName();
        Class<P> getType();
        C lookup(U user, P value);
    }

    public static interface ParameterUnwrapper<C, T>
    {
        T unwrap(C wrapped);
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

    public abstract static class AbstractParameterLookup<C, P, U> implements ParameterLookup<C, P, U>
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

    public abstract static class AbstractStringParameterLookup<C, U> extends AbstractParameterLookup<C, String, U>
    {
        public AbstractStringParameterLookup(String paramName)
        {
            super(paramName, String.class);
        }
    }

    public abstract static class AbstractKeyParameterLookup<C, U> extends AbstractStringParameterLookup<C, U>
    {
        public AbstractKeyParameterLookup()
        {
            super(KEY_FIELD_NAME);
        }
    }

    public abstract static class AbstractLongParameterLookup<C, U> extends AbstractParameterLookup<C, Long, U>
    {
        public AbstractLongParameterLookup(String paramName)
        {
            super(paramName, Long.class);
        }
    }

    public abstract static class AbstractIdParameterLookup<C, U> extends AbstractLongParameterLookup<C, U>
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

    private Optional<LookupProxy> createLookup(Optional<Map> containerMap) throws MalformedRequestException
    {
        Optional<LookupProxy> result = Optional.absent();
        if (containerMap.isPresent())
        {
            for(ParameterLookup<C, ?, U> parameterLookup : parameterLookups)
            {
                result = result.or(optionalLookup(createLookupFor(containerMap.get(), parameterLookup)));
            }
        }
        return result;
    }

    private <P> Optional<? extends Lookup> createLookupFor(Map<?, ?> params, final ParameterLookup<C, P, U> parameterLookup) throws MalformedRequestException
    {
        final String paramName = parameterLookup.getParamName();
        final Class<P> type = parameterLookup.getType();
        return Number.class.isAssignableFrom(type) ? createLookupForLong(params, paramName,
                (ParameterLookup<C, Long, U>) parameterLookup)
                : createLookupForNonLong(params, paramName, parameterLookup, type);
    }

    private <P> Optional<? extends Lookup> createLookupForNonLong(Map<?, ?> params, String paramName,
                                                       final ParameterLookup<C, P, U> parameterLookup, final Class<P> cls) throws MalformedRequestException
    {
        final Optional<P> value = getParam(params, paramName, cls);
        return value.isPresent() ? Optional.of(new Lookup<C,U>()
        {
            @Override
            public C lookup(U user)
            {
                return parameterLookup.lookup(user, value.get());
            }
        }) : Optional.<Lookup>absent();
    }

    // TODO: Can we avoid the code dupe here?
    private Optional<? extends Lookup> createLookupForLong(Map<?, ?> params, String paramName,
                                                           final ParameterLookup<C, Long, U> parameterLookup) throws MalformedRequestException
    {
        final Optional<Number> value = getParam(params, paramName, Number.class);
        return value.isPresent() ? Optional.of(new Lookup<C,U>()
        {

            @Override
            public C lookup(U user)
            {
                return parameterLookup.lookup(user, value.get().longValue());
            }
        }) : Optional.<Lookup>absent();
    }



    private static interface Lookup<C, U>
    {
        C lookup(U user);
    }

    // This wrapper class unfortunately needed to get around generics issue. At least I can't get it to work otherwise
    // Otherwise you end up with Optional<? extends Lookup> and calling or on that won't compile
    private class LookupProxy implements Lookup<C, U>
    {
        private final Lookup<C,U> target;

        LookupProxy(Lookup<C,U> target)
        {
            this.target = target;
        }

        @Override
        public C lookup(U user)
        {
            return target.lookup(user);
        }

        public Optional<LookupProxy> option()
        {
            return Optional.of(this);
        }

    }

    private Optional<LookupProxy> optionalLookup(Optional<? extends Lookup> target)
    {
        return target.isPresent() ? new LookupProxy(target.get()).option() : Optional.<LookupProxy>absent();
    }

}
