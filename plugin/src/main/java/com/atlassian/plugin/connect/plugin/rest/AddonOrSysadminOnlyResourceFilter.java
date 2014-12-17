package com.atlassian.plugin.connect.plugin.rest;

import com.atlassian.jwt.JwtConstants;
import com.atlassian.plugins.rest.common.security.AuthenticationRequiredException;
import com.atlassian.plugins.rest.common.security.AuthorisationException;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.rest.ConnectRestConstants.ADDON_KEY_PATH_PARAMETER;

/**
 * A Jersey resource filter used to require authentication as either an add-on or as a system administrator.
 *
 * @see com.atlassian.plugins.rest.common.security.jersey.SysadminOnlyResourceFilter
 */
@Provider
public class AddonOrSysadminOnlyResourceFilter implements ResourceFilter
{
    // TODO Figure out why a NoSuchBeanDefinitionException is thrown when these properties are injected through the constructor
    @Context
    HttpServletRequest httpRequest;

    @Context
    UriInfo uriInfo;

    private final UserManager userManager;

    public AddonOrSysadminOnlyResourceFilter(UserManager userManager)
    {
        this.userManager = Preconditions.checkNotNull(userManager);
    }

    @VisibleForTesting
    AddonOrSysadminOnlyResourceFilter(UserManager userManager, HttpServletRequest httpRequest, UriInfo uriInfo)
    {
        this(userManager);
        this.httpRequest = Preconditions.checkNotNull(httpRequest);
        this.uriInfo = Preconditions.checkNotNull(uriInfo);
    }

    public ContainerRequestFilter getRequestFilter()
    {
        return new AddonOrSysadminOnlyResourceFilter.RequestFilter();
    }

    public ContainerResponseFilter getResponseFilter()
    {
        return null;
    }

    private class RequestFilter implements ContainerRequestFilter
    {

        @Override
        public ContainerRequest filter(ContainerRequest containerRequest)
        {
            Object pluginKey = httpRequest.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME);
            if (pluginKey != null)
            {
                this.assertResourceAllowedForAddon(pluginKey);
            }
            else
            {
                UserKey userKey = userManager.getRemoteUserKey();
                if (userKey != null)
                {
                    this.assertUserIsSystemAdmin(userKey);
                }
                else
                {
                    throw new AuthenticationRequiredException();
                }
            }

            return containerRequest;
        }

        private void assertResourceAllowedForAddon(Object pluginKey)
        {
            List<String> resourceAddonKeys = uriInfo.getPathParameters().get(ADDON_KEY_PATH_PARAMETER);
            if (resourceAddonKeys != null && !resourceAddonKeys.isEmpty()) {
                String resourceAddonKey = resourceAddonKeys.iterator().next();
                if (!pluginKey.equals(resourceAddonKey)) {
                    throw new AuthorisationException(null);
                }
            }
        }

        private void assertUserIsSystemAdmin(UserKey userKey)
        {
            if (!userManager.isSystemAdmin(userKey))
            {
                throw new AuthorisationException(null);
            }
        }
    }
}
