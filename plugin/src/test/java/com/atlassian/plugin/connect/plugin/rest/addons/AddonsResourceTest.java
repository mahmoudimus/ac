package com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnInstaller;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.license.LicenseStatus;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddonLicense;
import com.atlassian.plugin.connect.plugin.rest.data.RestInternalAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestLimitedAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestRelatedLinks;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.api.license.entity.Contact;
import com.atlassian.upm.api.license.entity.LicenseType;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
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

    @Before
    public void setup()
    {
        this.resource = new AddonsResource(this.addonRegistry, this.licenseRetriever, this.connectApplinkManager,
                this.connectAddonManager, this.connectAddOnInstaller, this.applicationProperties, this.userManager);
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
