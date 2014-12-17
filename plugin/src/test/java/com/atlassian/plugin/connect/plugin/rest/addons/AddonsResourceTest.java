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
import com.atlassian.plugin.connect.plugin.rest.data.RestAddonType;
import com.atlassian.plugin.connect.plugin.rest.data.RestRelatedLinks;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.upm.api.license.entity.LicenseType;
import com.atlassian.upm.api.license.entity.PluginLicense;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;

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

    @Before
    public void setup()
    {
        this.resource = new AddonsResource(this.addonRegistry, this.licenseRetriever, this.connectApplinkManager,
                this.connectAddonManager, this.connectAddOnInstaller, this.applicationProperties);
    }

    @Test
    public void shouldReturnNotFoundWhenRequestingInvalidAddon()
    {
        String key = "invalid-key";

        when(this.addonRegistry.getAddonBean(key)).thenReturn(Option.none(ConnectAddonBean.class));

        Response response = this.resource.getAddon(key);
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void shouldReturnAddon()
    {
        String key = "my-addon-key";
        String version = "0.1";
        PluginState state = PluginState.INSTALLED;
        LicenseStatus licenseStatus = LicenseStatus.ACTIVE;
        LicenseType licenseType = LicenseType.DEVELOPER;
        boolean isEvaluationLicense = false;

        ConnectAddonBean beanMock = mock(ConnectAddonBean.class);
        when(beanMock.getKey()).thenReturn(key);
        when(beanMock.getVersion()).thenReturn(version);

        PluginLicense licenseMock = mock(PluginLicense.class);
        when(licenseMock.getLicenseType()).thenReturn(licenseType);
        when(licenseMock.isEvaluation()).thenReturn(isEvaluationLicense);

        when(this.addonRegistry.getAddonBean(key)).thenReturn(Option.some(beanMock));
        when(this.addonRegistry.getRestartState(key)).thenReturn(state);
        when(this.licenseRetriever.getLicense(key)).thenReturn(com.atlassian.upm.api.util.Option.some(licenseMock));
        when(this.licenseRetriever.getLicenseStatus(key)).thenReturn(licenseStatus);
        when(this.applicationProperties.getBaseUrl(UrlMode.CANONICAL)).thenReturn("http://localhost:2990/jira");

        Response response = this.resource.getAddon(key);
        RestAddon addon = (RestAddon) response.getEntity();

        assertThat(addon.getKey(), equalTo(key));
        assertThat(addon.getVersion(), equalTo(version));
        assertThat(addon.getType(), equalTo(RestAddonType.JSON));
        assertThat(addon.getState(), equalTo(state.name()));

        RestAddonLicense license = addon.getLicense();
        assertThat(license.getStatus(), equalTo(licenseStatus));
        assertThat(license.getType(), equalTo(licenseType));
        assertThat(license.isEvaluation(), equalTo(isEvaluationLicense));

        assertThat(addon.getApplink(), nullValue());
        assertThat(addon.getLinks(), instanceOf(RestRelatedLinks.class));
    }
}
