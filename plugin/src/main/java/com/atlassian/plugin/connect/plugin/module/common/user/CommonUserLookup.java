package com.atlassian.plugin.connect.plugin.module.common.user;

/**
 * Simple abstraction for some lookup operations that can be supported across products
 *
 * @param <U> the user type
 */
public interface CommonUserLookup<U>
{
    U lookupByUsername(String username) throws UserLookupException;
}
