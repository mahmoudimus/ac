package com.atlassian.plugin.connect.plugin.iframe.context;

import com.google.common.base.Strings;

/**
 * @since 1.0
 */
public abstract class AbstractModuleContextFilter<T> implements ModuleContextFilter
{
    public static final String PROFILE_NAME = "profileUser.name";
    public static final String PROFILE_KEY  = "profileUser.key";

    @Override
    public ModuleContextParameters filter(final ModuleContextParameters unfiltered)
    {
        final ModuleContextParameters filtered = new HashMapModuleContextParameters();
        T currentUser = getCurrentUser();
        for (PermissionCheck<T> permissionCheck : getPermissionChecks())
        {
            String value = unfiltered.get(permissionCheck.getParameterName());
            if (!Strings.isNullOrEmpty(value) && permissionCheck.hasPermission(value, currentUser))
            {
                filtered.put(permissionCheck.getParameterName(), value);
            }
        }
        return filtered;
    }

    /**
     * @return the context user, or {@code null} if the context request is executing anonymously.
     */
    protected abstract T getCurrentUser();

    /**
     * @return the {@link PermissionCheck permission checks} to be run over the unfiltered context.
     */
    protected abstract Iterable<PermissionCheck<T>> getPermissionChecks();

}
