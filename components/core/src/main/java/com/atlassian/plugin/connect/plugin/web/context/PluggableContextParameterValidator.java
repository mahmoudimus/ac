package com.atlassian.plugin.connect.plugin.web.context;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Options;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.spi.module.ContextParametersValidator;
import com.atlassian.plugin.connect.spi.module.CurrentUserProvider;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterResolverModuleDescriptor;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

@Component
public class PluggableContextParameterValidator
{

    private static final Predicate<String> IS_NOT_EMPTY = s -> !Strings.isNullOrEmpty(s);

    private final PluginAccessor pluginAccessor;
    private CurrentUserProvider currentUserProvider;

    @Autowired
    public PluggableContextParameterValidator(final PluginAccessor pluginAccessor, CurrentUserProvider currentUserProvider)
    {
        this.pluginAccessor = pluginAccessor;
        this.currentUserProvider = currentUserProvider;
    }

    public Map<String, String> filter(final Map<String, String> unfilteredContext)
    {
        final Map<String, String> filtered = new HashMap<>();
        final Object currentUser = currentUserProvider.getCurrentUser();

        Multimap<String, PermissionCheck> permissionChecksMultimap = getFieldNameToPermissionChecksMap();

        for (final String parameterName : Iterables.filter(unfilteredContext.keySet(), IS_NOT_EMPTY))
        {
            final String parameterValue = unfilteredContext.get(parameterName);
            Collection<PermissionCheck> permissionChecks = permissionChecksMultimap.get(parameterName);

            boolean allValidatorsGrantedPermission = Iterables.all(permissionChecks, new Predicate<PermissionCheck>()
            {
                @Override
                public boolean apply(final PermissionCheck userPermissionCheck)
                {

                    return userPermissionCheck.hasPermission(parameterValue, currentUser);
                }
            });

            if (allValidatorsGrantedPermission)
            {
                filtered.put(parameterName, parameterValue);
            }
        }

        return filtered;
    }

    private Multimap<String, PermissionCheck> getFieldNameToPermissionChecksMap()
    {
        ImmutableMultimap.Builder<String, PermissionCheck> result = ImmutableMultimap.builder();
        for (PermissionCheck userPermissionCheck : getAllPermissionChecks())
        {
            result.put(userPermissionCheck.getParameterName(), userPermissionCheck);
        }
        return result.build();
    }

    private Iterable<PermissionCheck> getAllPermissionChecks()
    {
        return concat(transform(getValidatorsFromPlugins(), new Function<ContextParametersValidator, Iterable<PermissionCheck>>()
        {
            @Override
            public Iterable<PermissionCheck> apply(final ContextParametersValidator validator)
            {
                return transform(validator.getPermissionChecks(), new Function<PermissionCheck, PermissionCheck>()
                {
                    @Override
                    public PermissionCheck apply(final PermissionCheck permissionCheck)
                    {
                        return new SafePermissionCheckFromPlugIn(permissionCheck);
                    }
                });
            }
        }));
    }

    private Iterable<ContextParametersValidator> getValidatorsFromPlugins()
    {
        Iterable<ContextParametersValidator> validators = Iterables.concat(Iterables.transform(pluginAccessor.getModules(
                        new ModuleDescriptorOfClassPredicate<>(ConnectContextParameterResolverModuleDescriptor.class)),
                new Function<ConnectContextParameterResolverModuleDescriptor.ConnectContextParametersResolver, List<ContextParametersValidator>>() {
                    @Override
                    public List<ContextParametersValidator> apply(final ConnectContextParameterResolverModuleDescriptor.ConnectContextParametersResolver input)
                    {
                        return input.getValidators();
                    }
                }));
        return Options.flatten(Iterables.transform(validators, new Function<ContextParametersValidator, Option<ContextParametersValidator>>()
        {
            @Override
            public Option<ContextParametersValidator> apply(final ContextParametersValidator contextParametersValidator)
            {
                return tryCast(contextParametersValidator);
            }
        }));
    }

    private Option<ContextParametersValidator> tryCast(ContextParametersValidator<?> unidentifiedValidator)
    {
        if (unidentifiedValidator.getUserType().equals(currentUserProvider.getUserType()))
        {
            @SuppressWarnings ("unchecked") // this suppression is safe, because above we checked that user types match
                    ContextParametersValidator castedModule = (ContextParametersValidator) unidentifiedValidator;
            return some(castedModule);
        }
        return none();
    }

    /**
     * This is a wrapper for permission checks from plug-ins.
     * If anything blows up we just keep calm, catch the exception and carry on.
     */
    private static class SafePermissionCheckFromPlugIn implements PermissionCheck
    {

        private static final Logger log = LoggerFactory.getLogger(SafePermissionCheckFromPlugIn.class);

        private final PermissionCheck wrappedPermissionCheck;

        private SafePermissionCheckFromPlugIn(final PermissionCheck wrappedPermissionCheck)
        {
            this.wrappedPermissionCheck = wrappedPermissionCheck;
        }

        @Override
        public String getParameterName()
        {
            try
            {
                return wrappedPermissionCheck.getParameterName();
            }
            catch (Throwable ex)
            {
                log.error("Error in " + wrappedPermissionCheck + " permission check", ex);
                return "";
            }
        }

        @Override
        public boolean hasPermission(final String value, final Object user)
        {
            try
            {
                return wrappedPermissionCheck.hasPermission(value, user);
            }
            catch (Throwable ex)
            {
                log.error("Error in " + wrappedPermissionCheck + " permission check", ex);
                return false;
            }
        }
    }
}
