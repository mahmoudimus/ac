package com.atlassian.labs.remoteapps.modules.permissions;

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * A set of permissions
 */
public class Permissions
{
    private final Set<String> permissions;

    public Permissions(Set<String> permissions)
    {
        this.permissions = unmodifiableSet(permissions);
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }
}
