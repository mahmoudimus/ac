package com.atlassian.plugin.connect.spi.module;

/**
 * Permission check for a single context variable.
 *
 * @since 1.1.16
 */
public interface PermissionCheck<User>
{
    /**
     * Context parameter names that this check can validate.
     *
     * @return variable name
     */
    String getParameterName();

    /**
     * Method that checks if a user has permission to access the variable value.
     *
     * @param value value of the variable specified in {@link PermissionCheck#getParameterName()} method
     * @param user user of the product
     * @return true is the user has permission to access the variable specific value, false otherwise
     */
    boolean hasPermission(String value, User user);
}
