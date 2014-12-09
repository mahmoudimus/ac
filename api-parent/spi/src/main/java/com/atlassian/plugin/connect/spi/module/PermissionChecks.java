package com.atlassian.plugin.connect.spi.module;

import com.atlassian.annotations.PublicApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static factory methods to create simple {@link com.atlassian.plugin.connect.spi.module.PermissionCheck} instances.
 *
 * <p>
 *     It also contains some useful abstract implementations.
 * </p>
 *
 * @since 1.1.16
 */
@PublicApi
public final class PermissionChecks
{

    private PermissionChecks() {}

    /**
     * Permission check that always allows to access the specified variable.
     *
     * @param parameterName variable name
     * @param <T> user class
     * @return always true permission check for the variable
     */
    public static <T> PermissionCheck<T> alwaysAllowed(String parameterName)
    {
        return new AlwaysAllowed<T>(parameterName);
    }

    /**
     * Permission check that allows to access the specified variable if a user is logged-in.
     *
     * @param parameterName variable name
     * @param <T> user class
     * @return logged-in user permission check for the variable
     */
    public static <T> PermissionCheck<T> mustBeLoggedIn(String parameterName)
    {
        return new MustBeLoggedIn<T>(parameterName);
    }

    /**
     * Abstract permission check that expects variable value to be {@link java.lang.Long}
     *
     * @param <User> user class
     */
    public static abstract class LongValue<User> implements PermissionCheck<User>
    {
        private static final Logger log = LoggerFactory.getLogger(LongValue.class);

        @Override
        public boolean hasPermission(final String value, final User user)
        {
            long longValue;
            try
            {
                longValue = Long.parseLong(value);
            }
            catch (NumberFormatException e)
            {
                log.debug("Failed to parse " + getParameterName(), e);
                return false;
            }
            return hasPermission(longValue, user);
        }

        public abstract boolean hasPermission(long value, User user);
    }

    private static class AlwaysAllowed<User> implements PermissionCheck<User>
    {
        private final String parameterName;

        public AlwaysAllowed(String parameterName)
        {
            this.parameterName = parameterName;
        }

        @Override
        public String getParameterName()
        {
            return parameterName;
        }

        @Override
        public boolean hasPermission(final String value, final User user)
        {
            return true;
        }
    }

    private static class MustBeLoggedIn<User> extends AlwaysAllowed<User>
    {
        public MustBeLoggedIn(String parameterName)
        {
            super(parameterName);
        }

        @Override
        public boolean hasPermission(final String value, final User user)
        {
            return user != null;
        }
    }

}
