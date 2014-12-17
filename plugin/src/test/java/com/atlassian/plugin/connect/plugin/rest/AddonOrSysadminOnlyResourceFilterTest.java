package com.atlassian.plugin.connect.plugin.rest;

import com.atlassian.jwt.JwtConstants;
import com.atlassian.plugins.rest.common.security.AuthenticationRequiredException;
import com.atlassian.plugins.rest.common.security.AuthorisationException;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.Lists;
import com.sun.jersey.spi.container.ContainerRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.rest.ConnectRestConstants.ADDON_KEY_PATH_PARAMETER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddonOrSysadminOnlyResourceFilterTest
{
    private AddonOrSysadminOnlyResourceFilter resourceFilter;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UserManager userManager;

    @Mock
    private ContainerRequest containerRequest;

    @Mock
    private Map containerRequestProperties;

    @Mock
    private MultivaluedMap<String, String> pathParameters;

    @Before
    public void setup()
    {
        this.resourceFilter = new AddonOrSysadminOnlyResourceFilter(this.userManager, this.httpRequest, this.uriInfo);
    }

    @Test(expected = AuthenticationRequiredException.class)
    public void shouldRejectAnonymousRequest()
    {
        when(this.userManager.getRemoteUserKey()).thenReturn(null);

        this.resourceFilter.getRequestFilter().filter(this.containerRequest);
    }

    @Test(expected = AuthorisationException.class)
    public void shouldRejectUserRequest()
    {
        UserKey userKey = new UserKey("charlie");

        when(this.userManager.getRemoteUserKey()).thenReturn(userKey);
        when(this.userManager.isSystemAdmin(userKey)).thenReturn(false);

        this.resourceFilter.getRequestFilter().filter(this.containerRequest);
    }

    @Test
    public void shouldAllowSystemAdminRequest()
    {
        UserKey userKey = new UserKey("charlie");

        when(this.userManager.getRemoteUserKey()).thenReturn(userKey);
        when(this.userManager.isSystemAdmin(userKey)).thenReturn(true);

        assertThat(this.resourceFilter.getRequestFilter().filter(this.containerRequest), equalTo(this.containerRequest));
    }

    @Test
    public void shouldAllowAddonRequestToNonAddonResource()
    {
        String pluginKey = "my-addon";

        when(this.httpRequest.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(pluginKey);
        when(this.uriInfo.getPathParameters()).thenReturn(this.pathParameters);
        when(this.pathParameters.get(ADDON_KEY_PATH_PARAMETER)).thenReturn(Collections.EMPTY_LIST);

        assertThat(this.resourceFilter.getRequestFilter().filter(this.containerRequest), equalTo(this.containerRequest));
    }

    @Test
    public void shouldAllowAddonRequestToResourceForSameAddOn()
    {
        String pluginKey = "my-addon";

        when(this.httpRequest.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(pluginKey);
        when(this.uriInfo.getPathParameters()).thenReturn(this.pathParameters);
        when(this.pathParameters.get(ADDON_KEY_PATH_PARAMETER)).thenReturn(Lists.newArrayList(pluginKey));

        assertThat(this.resourceFilter.getRequestFilter().filter(this.containerRequest), equalTo(this.containerRequest));
    }

    @Test(expected = AuthorisationException.class)
    public void shouldRejectAddonRequestToResourceForOtherAddOn()
    {
        String pluginKey = "my-addon";
        String otherPluginKey = "other-addon";

        when(this.httpRequest.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(pluginKey);
        when(this.uriInfo.getPathParameters()).thenReturn(this.pathParameters);
        when(this.pathParameters.get(ADDON_KEY_PATH_PARAMETER)).thenReturn(Lists.newArrayList(otherPluginKey));

        assertThat(this.resourceFilter.getRequestFilter().filter(this.containerRequest), equalTo(this.containerRequest));
    }
}
