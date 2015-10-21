package com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.spi.installer.ConnectAddOnInstaller;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddonsResourceTest
{

    @InjectMocks
    private AddonsResource resource;

    @Mock
    private ConnectAddonRegistry addonRegistry;

    @Mock
    private LicenseRetriever licenseRetriever;

    @Mock
    private ConnectApplinkManager connectApplinkManager;

    @Mock
    private ConnectAddonManager connectAddonManager;

    @Mock
    private ConnectAddOnInstaller connectAddOnInstaller;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private ConnectAddonAccessor addonAccessor;

    @Mock
    private UserManager userManager;

    @Mock
    private ProductAccessor productAccessor;

    @Test
    public void shouldReturnNotFoundWhenRequestingInvalidAddon()
    {
        String key = "invalid-key";

        when(addonAccessor.getAddon(key)).thenReturn(Optional.empty());

        Response response = resource.getAddon(key);
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
    }
}
