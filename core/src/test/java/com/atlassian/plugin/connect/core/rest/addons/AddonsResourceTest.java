package com.atlassian.plugin.connect.core.rest.addons;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.core.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.core.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.core.license.LicenseRetriever;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.spi.installer.ConnectAddOnInstaller;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddonsResourceTest
{

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
    private UserManager userManager;

    @Mock
    private ProductAccessor productAccessor;

    @Before
    public void setup()
    {
        this.resource = new AddonsResource(this.addonRegistry, this.licenseRetriever, this.connectApplinkManager,
                this.connectAddonManager, this.connectAddOnInstaller, this.applicationProperties, this.userManager,
                this.productAccessor);
    }

    @Test
    public void shouldReturnNotFoundWhenRequestingInvalidAddon()
    {
        String key = "invalid-key";

        when(addonRegistry.getAddonBean(key)).thenReturn(Option.none(ConnectAddonBean.class));

        Response response = resource.getAddon(key);
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
    }
}
