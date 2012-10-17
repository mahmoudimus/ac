package com.atlassian.plugin.remotable.container.service.sal;

import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.sal.api.user.UserResolutionException;

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

    @Override
    public String getRemoteUsername(HttpServletRequest request)
    {
        return requestContext.getUserId();
    }

    @Override
    public UserProfile getUserProfile(String username)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInGroup(String username, String group)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSystemAdmin(String username)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAdmin(String username)
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
}
