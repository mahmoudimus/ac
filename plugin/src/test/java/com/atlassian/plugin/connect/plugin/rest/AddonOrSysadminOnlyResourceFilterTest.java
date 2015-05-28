package com.atlassian.plugin.connect.plugin.rest;

import com.atlassian.jwt.JwtConstants;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
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

    @Test(expected = ConnectAddonAuthenticationRequiredException.class)
    public void shouldRejectAnonymousRequest()
    {
        when(userManager.getRemoteUserKey()).thenReturn(null);

        resourceFilter.getRequestFilter().filter(containerRequest);
    }

    @Test(expected = PermissionDeniedException.class)
    public void shouldRejectUserRequest()
    {
        UserKey userKey = new UserKey("charlie");

        when(userManager.getRemoteUserKey()).thenReturn(userKey);
        when(userManager.isSystemAdmin(userKey)).thenReturn(false);

        resourceFilter.getRequestFilter().filter(containerRequest);
    }

    @Test
    public void shouldAllowSystemAdminRequest()
    {
        UserKey userKey = new UserKey("charlie");

        when(userManager.getRemoteUserKey()).thenReturn(userKey);
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);

        assertThat(resourceFilter.getRequestFilter().filter(containerRequest), equalTo(containerRequest));
    }

    @Test
    public void shouldAllowAddonRequestToNonAddonResource()
    {
        String pluginKey = "my-addon";

        when(httpRequest.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(pluginKey);
        when(uriInfo.getPathParameters()).thenReturn(pathParameters);
        when(pathParameters.get(ADDON_KEY_PATH_PARAMETER)).thenReturn(Collections.EMPTY_LIST);

        assertThat(resourceFilter.getRequestFilter().filter(containerRequest), equalTo(containerRequest));
    }

    @Test
    public void shouldAllowAddonRequestToResourceForSameAddOn()
    {
        String pluginKey = "my-addon";

        when(httpRequest.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(pluginKey);
        when(uriInfo.getPathParameters()).thenReturn(pathParameters);
        when(pathParameters.get(ADDON_KEY_PATH_PARAMETER)).thenReturn(Lists.newArrayList(pluginKey));

        assertThat(resourceFilter.getRequestFilter().filter(containerRequest), equalTo(containerRequest));
    }

    @Test(expected = PermissionDeniedException.class)
    public void shouldRejectAddonRequestToResourceForOtherAddOn()
    {
        String pluginKey = "my-addon";
        String otherPluginKey = "other-addon";

        when(httpRequest.getAttribute(JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME)).thenReturn(pluginKey);
        when(uriInfo.getPathParameters()).thenReturn(pathParameters);
        when(pathParameters.get(ADDON_KEY_PATH_PARAMETER)).thenReturn(Lists.newArrayList(otherPluginKey));

        assertThat(resourceFilter.getRequestFilter().filter(containerRequest), equalTo(containerRequest));
    }
}
