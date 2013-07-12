package com.atlassian.plugin.remotable.container.service.sal;

import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.sal.api.user.UserResolutionException;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 *
 */
public class ContainerUserManager implements UserManager
{
    private final RequestContext requestContext;

    public ContainerUserManager(RequestContext requestContext)
    {
        this.requestContext = requestContext;
    }

    @Override
    public String getRemoteUsername()
    {
        return requestContext.getUserId();
    }

    @Nullable
    @Override
    public UserProfile getRemoteUser()
    {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public UserKey getRemoteUserKey()
    {
        return new UserKey(requestContext.getUserId());
    }

    @Override
    public String getRemoteUsername(HttpServletRequest request)
    {
        return requestContext.getUserId();
    }

    @Nullable
    @Override
    public UserProfile getRemoteUser(HttpServletRequest httpServletRequest)
    {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public UserKey getRemoteUserKey(HttpServletRequest httpServletRequest)
    {
        return getRemoteUserKey();
    }

    @Override
    public UserProfile getUserProfile(String username)
    {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public UserProfile getUserProfile(@Nullable UserKey userKey)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInGroup(String username, String group)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInGroup(@Nullable UserKey userKey, @Nullable String group)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSystemAdmin(String username)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSystemAdmin(@Nullable UserKey userKey)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAdmin(String username)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAdmin(@Nullable UserKey userKey)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean authenticate(String username, String password)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal resolve(String username) throws UserResolutionException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<String> findGroupNamesByPrefix(String s, int startIndex, int maxResults)
    {
        throw new UnsupportedOperationException();
    }
}
