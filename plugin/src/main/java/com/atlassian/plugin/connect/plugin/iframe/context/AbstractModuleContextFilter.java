package com.atlassian.plugin.connect.plugin.iframe.context;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.iframe.context.module.ConnectContextVariablesValidatorModuleDescriptor;
import com.atlassian.plugin.connect.spi.module.ContextVariablesValidator;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

/**
 * @since 1.0
 */
public abstract class AbstractModuleContextFilter<User> implements ModuleContextFilter
{
    public static final String PROFILE_NAME = "profileUser.name";
    public static final String PROFILE_KEY = "profileUser.key";

    private final Set<ContextVariablesValidator<User>> contextVariablesValidatorsFromPlugins = new CopyOnWriteArraySet<ContextVariablesValidator<User>>();

    private final ResettableLazyReference<Iterable<PermissionCheck<User>>> allPermissionChecks = new ResettableLazyReference<Iterable<PermissionCheck<User>>>()
    {
        @Override
        protected Iterable<PermissionCheck<User>> create() throws Exception
        {
            return getAllPermissionChecks();
        }
    };

    protected AbstractModuleContextFilter(final PluginAccessor pluginAccessor, final PluginEventManager pluginEventManager, final Class<User> userType)
    {
        DefaultPluginModuleTracker<ContextVariablesValidator<?>, ConnectContextVariablesValidatorModuleDescriptor> tracker = new DefaultPluginModuleTracker<ContextVariablesValidator<?>, ConnectContextVariablesValidatorModuleDescriptor>(
                pluginAccessor, pluginEventManager, ConnectContextVariablesValidatorModuleDescriptor.class, new PluginModuleTracker.Customizer<ContextVariablesValidator<?>, ConnectContextVariablesValidatorModuleDescriptor>()
        {

            @Override
            public ConnectContextVariablesValidatorModuleDescriptor adding(final ConnectContextVariablesValidatorModuleDescriptor descriptor)
            {

                tryCast(descriptor.getModule()).foreach(new Effect<ContextVariablesValidator<User>>()
                {
                    @Override
                    public void apply(final ContextVariablesValidator<User> validator)
                    {
                        contextVariablesValidatorsFromPlugins.add(validator);
                        allPermissionChecks.reset();
                    }
                });

                return descriptor;
            }

            @Override
            public void removed(final ConnectContextVariablesValidatorModuleDescriptor descriptor)
            {
                tryCast(descriptor.getModule()).foreach(new Effect<ContextVariablesValidator<User>>()
                {
                    @Override
                    public void apply(final ContextVariablesValidator<User> validator)
                    {
                        contextVariablesValidatorsFromPlugins.remove(validator);
                        allPermissionChecks.reset();
                    }
                });
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
        });

    }

    @Override
    public ModuleContextParameters filter(final ModuleContextParameters unfiltered)
    {
        final ModuleContextParameters filtered = new HashMapModuleContextParameters();
        User currentUser = getCurrentUser();
        for (PermissionCheck<User> permissionCheck : allPermissionChecks.get())
        {
            String value = unfiltered.get(permissionCheck.getParameterName());
            if (!Strings.isNullOrEmpty(value) && permissionCheck.hasPermission(value, currentUser))
            {
                filtered.put(permissionCheck.getParameterName(), value);
            }
        }
        return filtered;
    }

    private Iterable<PermissionCheck<User>> getAllPermissionChecks()
    {
        return concat(getPermissionChecks(), concat(transform(contextVariablesValidatorsFromPlugins, new Function<ContextVariablesValidator<User>, Iterable<PermissionCheck<User>>>()
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
