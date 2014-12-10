package com.atlassian.plugin.connect.plugin.iframe.context;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Options;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.iframe.context.module.ConnectContextVariablesValidatorModuleDescriptor;
import com.atlassian.plugin.connect.spi.module.ContextVariablesValidator;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import javax.annotation.Nullable;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

/**
 * @since 1.0
 */
public abstract class AbstractModuleContextFilter<User> implements ModuleContextFilter
{
    public static final String PROFILE_NAME = "profileUser.name";
    public static final String PROFILE_KEY = "profileUser.key";

    private static final Predicate<String> IS_NOT_EMPTY = new Predicate<String>()
    {
        @Override
        public boolean apply(@Nullable final String s)
        {
            return !Strings.isNullOrEmpty(s);
        }
    };

    private final PluginAccessor pluginAccessor;
    private final Class<User> userType;

    protected AbstractModuleContextFilter(final PluginAccessor pluginAccessor, final Class<User> userType)
    {

        this.pluginAccessor = pluginAccessor;
        this.userType = userType;

    }

    @Override
    public ModuleContextParameters filter(final ModuleContextParameters unfiltered)
    {
        final ModuleContextParameters filtered = new HashMapModuleContextParameters();
        final User currentUser = getCurrentUser();

        Multimap<String, PermissionCheck<User>> permissionChecksMultimap = getFieldNameToPermissionChecksMap();

        for (final String parameterName : Iterables.filter(unfiltered.keySet(), IS_NOT_EMPTY))
        {
            final String parameterValue = unfiltered.get(parameterName);
            Collection<PermissionCheck<User>> permissionChecks = permissionChecksMultimap.get(parameterName);

            boolean allValidatorsGrantedPermission = Iterables.all(permissionChecks, new Predicate<PermissionCheck<User>>()
            {
                @Override
                public boolean apply(final PermissionCheck<User> userPermissionCheck)
                {

                    return userPermissionCheck.hasPermission(parameterValue, currentUser);
                }
            });

            if (!permissionChecks.isEmpty() && allValidatorsGrantedPermission)
            {
                filtered.put(parameterName, parameterValue);
            }
        }

        return filtered;
    }

    private Multimap<String, PermissionCheck<User>> getFieldNameToPermissionChecksMap()
    {
        ImmutableMultimap.Builder<String, PermissionCheck<User>> result = ImmutableMultimap.builder();
        for (PermissionCheck<User> userPermissionCheck : getAllPermissionChecks())
        {
            result.put(userPermissionCheck.getParameterName(), userPermissionCheck);
        }
        return result.build();
    }

    private Iterable<PermissionCheck<User>> getAllPermissionChecks()
    {
        return concat(getPermissionChecks(), concat(transform(getValidatorsFromPlugins(), new Function<ContextVariablesValidator<User>, Iterable<PermissionCheck<User>>>()
        {
            @Override
            public Iterable<PermissionCheck<User>> apply(final ContextVariablesValidator<User> validator)
            {
                return transform(validator.getPermissionChecks(), new Function<PermissionCheck<User>, PermissionCheck<User>>()
                {
                    @Override
                    public PermissionCheck<User> apply(final PermissionCheck<User> permissionCheck)
                    {
                        return new SafePermissionCheckFromPlugIn<User>(permissionCheck);
                    }
                });
            }
        })));
    }

    private Iterable<ContextVariablesValidator<User>> getValidatorsFromPlugins()
    {
        Collection<ContextVariablesValidator<?>> validators = pluginAccessor.getModules(new ModuleDescriptorOfClassPredicate<ContextVariablesValidator<?>>(ConnectContextVariablesValidatorModuleDescriptor.class));
        return Options.flatten(Iterables.transform(validators, new Function<ContextVariablesValidator<?>, Option<ContextVariablesValidator<User>>>()
        {
            @Override
            public Option<ContextVariablesValidator<User>> apply(final ContextVariablesValidator<?> contextVariablesValidator)
            {
                return tryCast(contextVariablesValidator);
            }
        }));
    }

    private Option<ContextVariablesValidator<User>> tryCast(ContextVariablesValidator<?> unidentifiedValidator)
    {
        if (unidentifiedValidator.getUserType().equals(userType))
        {
            @SuppressWarnings ("unchecked") // this supression is safe, because above we checked that user types match
                    ContextVariablesValidator<User> castedModule = (ContextVariablesValidator<User>) unidentifiedValidator;
            return some(castedModule);
        }
        return none();
    }

    /**
     * @return the context user, or {@code null} if the context request is executing anonymously.
     */
    protected abstract User getCurrentUser();

    /**
     * @return the {@link PermissionCheck permission checks} to be run over the unfiltered context.
     */
    protected abstract Iterable<PermissionCheck<User>> getPermissionChecks();

    /**
     * This is a wrappeer for permission checks from plug-ins.
     * If anything blows up we just keep calm, catch an exception and carry on.
     */
    private static class SafePermissionCheckFromPlugIn<User> implements PermissionCheck<User>
    {

        private static final Logger log = LoggerFactory.getLogger(SafePermissionCheckFromPlugIn.class);

        private final PermissionCheck<User> wrappedPermissionCheck;

        private SafePermissionCheckFromPlugIn(final PermissionCheck<User> wrappedPermissionCheck)
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
        public boolean hasPermission(final String value, final User user)
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
