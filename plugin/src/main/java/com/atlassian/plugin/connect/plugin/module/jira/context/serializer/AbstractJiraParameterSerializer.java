package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.context.ParameterDeserializer;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.google.common.base.Optional;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractJiraParameterSerializer<T, C> implements ParameterSerializer<T>, ParameterDeserializer<T>
{
    public static final String ID_FIELD_NAME = "id";
    public static final String KEY_FIELD_NAME = "key";
    private final UserManager userManager;
    private final String containerFieldName;
    private final ParameterLookup<C, ?>[] parameterLookups;
    private final ParameterUnwrapper<C, T> parameterUnwrapper;

    // TODO: abstract a common user interface across products to support simple lookup by username
    public AbstractJiraParameterSerializer(UserManager userManager, String containerFieldName,
                                           ParameterUnwrapper<C, T> parameterUnwrapper,
                                           ParameterLookup<C, ?>... parameterLookups)
    {
        this.parameterUnwrapper = parameterUnwrapper;
        this.userManager = checkNotNull(userManager, "userManager is mandatory");
        this.containerFieldName = checkNotNull(containerFieldName);
        this.parameterLookups = parameterLookups;
    }

    @Override
    public Optional<T> deserialize(Map<String, Object> params, String username)
    {
        final Optional<Map> containerMap = getParam(params, containerFieldName, Map.class);
        if (!containerMap.isPresent())
        {
            return Optional.absent();
        }

        final Optional<LookupProxy> lookup = createLookup(containerMap);
        if (!lookup.isPresent())
        {
            return Optional.absent();
        }

        final ApplicationUser appUser = userManager.getUserByName(username);
        if (appUser == null)
        {
            // TODO: Should this be an exception?
            return Optional.absent();
        }

        final User user = appUser.getDirectoryUser();

        final C serviceResult = lookup.get().lookup(user);

        if (serviceResult == null || (serviceResult instanceof ServiceResult && !((ServiceResult)serviceResult).isValid()))
        {
            // TODO: Should this be an exception?
            return Optional.absent();
        }

        return Optional.of(parameterUnwrapper.unwrap(serviceResult));
    }

    public static interface ParameterLookup<C, P>
    {
        String getParamName();
        Class<P> getType();
        C lookup(User user, P value);
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

    public abstract static class AbstractParameterLookup<C, P> implements ParameterLookup<C, P>
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

    public abstract static class AbstractStringParameterLookup<C> extends AbstractParameterLookup<C, String>
    {
        public AbstractStringParameterLookup(String paramName)
        {
            super(paramName, String.class);
        }
    }

    public abstract static class AbstractKeyParameterLookup<C> extends AbstractStringParameterLookup<C>
    {
        public AbstractKeyParameterLookup()
        {
            super(KEY_FIELD_NAME);
        }
    }

    public abstract static class AbstractLongParameterLookup<C> extends AbstractParameterLookup<C, Long>
    {
        public AbstractLongParameterLookup(String paramName)
        {
            super(paramName, Long.class);
        }
    }

    public abstract static class AbstractIdParameterLookup<C> extends AbstractLongParameterLookup<C>
    {
        public AbstractIdParameterLookup()
        {
            super(ID_FIELD_NAME);
        }
    }

    private <C> Optional<C> getParam(Map<?, ?> params, String paramName, Class<C> type)
    {
        final Object o = params.get(paramName);
        if (o == null || !type.isInstance(o))
        {
            return Optional.absent();
        }
        else
        {
            return Optional.of((C) o);
        }
    }

    private Optional<LookupProxy> createLookup(Optional<Map> containerMap)
    {
        Optional<LookupProxy> result = Optional.absent();
        if (containerMap.isPresent())
        {
            for(ParameterLookup<C, ?> parameterLookup : parameterLookups)
            {
                result = result.or(optionalLookup(createLookupFor(containerMap.get(), parameterLookup)));
            }
        }
        return result;
    }

    private <P> Optional<? extends Lookup> createLookupFor(Map<?, ?> params, final ParameterLookup<C, P> parameterLookup)
    {
        final String paramName = parameterLookup.getParamName();
        final Class<P> type = parameterLookup.getType();
        return Number.class.isAssignableFrom(type) ? createLookupForLong(params, paramName, (ParameterLookup<C, Long>) parameterLookup)
                : createLookupForNonLong(params, paramName, parameterLookup, type);
    }

    private <P> Optional<? extends Lookup> createLookupForNonLong(Map<?, ?> params, String paramName,
                                                       final ParameterLookup<C, P> parameterLookup, final Class<P> cls)
    {
        final Optional<P> value = getParam(params, paramName, cls);
        return value.isPresent() ? Optional.of(new Lookup()
        {
            @Override
            public C lookup(User user)
            {
                return parameterLookup.lookup(user, value.get());
            }
        }) : Optional.<Lookup>absent();
    }

    // TODO: Can we avoid the code dupe here?
    private Optional<? extends Lookup> createLookupForLong(Map<?, ?> params, String paramName,
                                                           final ParameterLookup<C, Long> parameterLookup)
    {
        final Optional<Number> value = getParam(params, paramName, Number.class);
        return value.isPresent() ? Optional.of(new Lookup()
        {

            @Override
            public C lookup(User user)
            {
                return parameterLookup.lookup(user, value.get().longValue());
            }
        }) : Optional.<Lookup>absent();
    }


    private interface Lookup<C>
    {
        C lookup(User user);
    }



    // unfortunately needed to get around generics issue. At least I can't get it to work otherwise
    private class LookupProxy implements Lookup<C>
    {
        private final Lookup<C> target;

        LookupProxy(Lookup<C> target)
        {
            this.target = target;
        }

        @Override
        public C lookup(User user)
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
