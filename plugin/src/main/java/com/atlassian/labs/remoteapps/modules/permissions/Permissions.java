package com.atlassian.labs.remoteapps.modules.permissions;

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/27/12 Time: 8:34 AM To change this template use
 * File | Settings | File Templates.
 */
public class Permissions
{
    private final Set<String> permissions;

    public Permissions(Set<String> permissions)
    {
        //To change body of created methods use File | Settings | File Templates.
        this.permissions = unmodifiableSet(permissions);
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }
}
