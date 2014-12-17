package com.atlassian.plugin.connect.spi.module;

import com.atlassian.annotations.PublicSpi;

import java.util.Collection;

/**
 * Instances of this class can specify which context parameters
 * can be passed on to the Connect add-ons by implementing the
 * permission checks for those parameters.
 *
 * @param <User> a type of user this class can operate on, depends on a product, e.g. in Confluence this would be a {@code ConfluenceUser} and in JIRA {@code ApplicationUser}
 */
@PublicSpi
public interface ContextParametersValidator<User>
{
    /**
     * A collection of context variables permission checks.
     *
     * <p>
     *     If you want a parameter to be available in Connect context,
     *     you must add a permission check for this parameter. Even if
     *     it is always allowed.
     *
     * </p>
     *
     * <p>
     *     It's advisable to make use of static factory methods defined in {@link com.atlassian.plugin.connect.spi.module.PermissionChecks}.
     *
     *     <ul>
     *         <li>
     *             {@link PermissionChecks#alwaysAllowed(String)} -- when a variable is always allowed, you still need to add a permission check
     *         </li>
     *         <li>
     *             {@link com.atlassian.plugin.connect.spi.module.PermissionChecks#mustBeLoggedIn(String)} -- when all we require is a logged-in user.
     *         </li>
     *     </ul>
     * </p>
     *
     * @return a collection of permission checks
     */
    public Collection<PermissionCheck<User>> getPermissionChecks();

    /**
     * Return a class of user that this validator can operate on.
     *
     * <p>
     *     This method must be here because of Java generic type erasure.
     * </p>
     *
     * <p>
     *     Your validator will be applied to a different product depending on
     *     what you return here (and what generic type you use for the class).
     *
     *     <p>
     *         Currently there are two options:
     *         <ul>
     *             <li>JIRA: ApplicationUser</li>
     *             <li>Confluence: ConfluenceUser</li>
     *         </ul>
     *
     *
     *     </p>
     *
     * </p>
     *
     * @return a class of the user which permissions are validated
     */
    public Class<User> getUserType();
}
