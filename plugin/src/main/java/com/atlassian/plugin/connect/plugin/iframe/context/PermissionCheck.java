package com.atlassian.plugin.connect.plugin.iframe.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates common permission checking patterns used in implementations of {@link ModuleContextFilter}. If
 * {@link #hasPermission(String, Object)} returns true for a parameter {@link #getParameterName() name} and value in the
 * unfiltered context map, it will be added to the filtered context map.
 *
 * @since 1.0
 */
public interface PermissionCheck<T>
{
    String getParameterName();
    boolean hasPermission(String value, T user);

    static abstract class LongValue<T> implements PermissionCheck<T>
    {
        private static final Logger log = LoggerFactory.getLogger(LongValue.class);

        @Override
        public boolean hasPermission(final String value, final T user)
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

        public abstract boolean hasPermission(long value, T user);
    }

    static class AlwaysAllowed<T> implements PermissionCheck<T>
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
        public boolean hasPermission(final String value, final T user)
        {
            return true;
        }
    }

    static class MustBeLoggedIn<T> extends AlwaysAllowed<T>
    {
        public MustBeLoggedIn(String parameterName)
        {
            super(parameterName);
        }

        @Override
        public boolean hasPermission(final String value, final T user)
        {
            return user != null;
        }
    }
}
