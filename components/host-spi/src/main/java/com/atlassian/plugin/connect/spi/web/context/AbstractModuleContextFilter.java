package com.atlassian.plugin.connect.spi.web.context;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Options;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.spi.module.ContextParametersValidator;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

/**
 * @param <T> user type, depends on the product (e.g. ApplicationUser, ConfluenceUser)
 * @since 1.0
 */
public abstract class AbstractModuleContextFilter<T> implements ModuleContextFilter {
    public static final String PROFILE_NAME = "profileUser.name";
    public static final String PROFILE_KEY = "profileUser.key";

    private static final Predicate<String> IS_NOT_EMPTY = Predicates.not(Strings::isNullOrEmpty);

    private final PluginAccessor pluginAccessor;
    private final Class<T> userType;

    protected AbstractModuleContextFilter(final PluginAccessor pluginAccessor, final Class<T> userType) {
        this.pluginAccessor = pluginAccessor;
        this.userType = userType;
    }

    @Override
    public ModuleContextParameters filter(final ModuleContextParameters unfiltered) {
        final ModuleContextParameters filtered = new HashMapModuleContextParameters(unfiltered.getOriginalContext());
        final T currentUser = getCurrentUser();

        Multimap<String, PermissionCheck<T>> permissionChecksMultimap = getFieldNameToPermissionChecksMap();

        for (final String parameterName : Iterables.filter(unfiltered.keySet(), IS_NOT_EMPTY)) {
            final String parameterValue = unfiltered.get(parameterName);
            Collection<PermissionCheck<T>> permissionChecks = permissionChecksMultimap.get(parameterName);

            boolean allValidatorsGrantedPermission = Iterables.all(permissionChecks, userPermissionCheck -> userPermissionCheck.hasPermission(parameterValue, currentUser));

            if (!permissionChecks.isEmpty() && allValidatorsGrantedPermission) {
                filtered.put(parameterName, parameterValue);
            }
        }

        return filtered;
    }

    private Multimap<String, PermissionCheck<T>> getFieldNameToPermissionChecksMap() {
        ImmutableMultimap.Builder<String, PermissionCheck<T>> result = ImmutableMultimap.builder();
        for (PermissionCheck<T> userPermissionCheck : getAllPermissionChecks()) {
            result.put(userPermissionCheck.getParameterName(), userPermissionCheck);
        }
        return result.build();
    }

    private Iterable<PermissionCheck<T>> getAllPermissionChecks() {
        return concat(getPermissionChecks(), concat(transform(getValidatorsFromPlugins(), new Function<ContextParametersValidator<T>, Iterable<PermissionCheck<T>>>() {
            @Override
            public Iterable<PermissionCheck<T>> apply(final ContextParametersValidator<T> validator) {
                return transform(validator.getPermissionChecks(), new Function<PermissionCheck<T>, PermissionCheck<T>>() {
                    @Override
                    public PermissionCheck<T> apply(final PermissionCheck<T> permissionCheck) {
                        return new SafePermissionCheckFromPlugIn<T>(permissionCheck);
                    }
                });
            }
        })));
    }

    private Iterable<ContextParametersValidator<T>> getValidatorsFromPlugins() {
        Iterable<ContextParametersValidator> validators = Iterables.concat(Iterables.transform(pluginAccessor.getModules(
                new ModuleDescriptorOfClassPredicate<>(ConnectContextParameterResolverModuleDescriptor.class)),
                new Function<ConnectContextParameterResolverModuleDescriptor.ConnectContextParametersResolver, List<ContextParametersValidator>>() {
                    @Override
                    public List<ContextParametersValidator> apply(final ConnectContextParameterResolverModuleDescriptor.ConnectContextParametersResolver input) {
                        return input.getValidators();
                    }
                }));
        return Options.flatten(Iterables.transform(validators, AbstractModuleContextFilter.this::tryCast));
    }

    private Option<ContextParametersValidator<T>> tryCast(ContextParametersValidator<?> unidentifiedValidator) {
        if (unidentifiedValidator.getUserType().equals(userType)) {
            @SuppressWarnings("unchecked") // this suppression is safe, because above we checked that user types match
                    ContextParametersValidator<T> castedModule = (ContextParametersValidator<T>) unidentifiedValidator;
            return some(castedModule);
        }
        return none();
    }

    /**
     * @return the context user, or {@code null} if the context request is executing anonymously.
     */
    protected abstract T getCurrentUser();

    /**
     * @return the {@link PermissionCheck permission checks} to be run over the unfiltered context.
     */
    protected abstract Iterable<PermissionCheck<T>> getPermissionChecks();

    /**
     * This is a wrapper for permission checks from plug-ins.
     * If anything blows up we just keep calm, catch the exception and carry on.
     */
    private static class SafePermissionCheckFromPlugIn<T> implements PermissionCheck<T> {

        private static final Logger log = LoggerFactory.getLogger(SafePermissionCheckFromPlugIn.class);

        private final PermissionCheck<T> wrappedPermissionCheck;

        private SafePermissionCheckFromPlugIn(final PermissionCheck<T> wrappedPermissionCheck) {
            this.wrappedPermissionCheck = wrappedPermissionCheck;
        }

        @Override
        public String getParameterName() {
            try {
                return wrappedPermissionCheck.getParameterName();
            } catch (Throwable ex) {
                log.error("Error in " + wrappedPermissionCheck + " permission check", ex);
                return "";
            }
        }

        @Override
        public boolean hasPermission(final String value, final T user) {
            try {
                return wrappedPermissionCheck.hasPermission(value, user);
            } catch (Throwable ex) {
                log.error("Error in " + wrappedPermissionCheck + " permission check", ex);
                return false;
            }
        }
    }
}
