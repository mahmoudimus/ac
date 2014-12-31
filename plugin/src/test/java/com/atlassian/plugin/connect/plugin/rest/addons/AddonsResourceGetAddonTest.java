package com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.extras.api.Contact;
import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.ProductLicense;
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
import com.atlassian.plugin.connect.plugin.rest.data.RestHost;
import com.atlassian.plugin.connect.plugin.rest.data.RestInternalAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestLimitedAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestRelatedLinks;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.api.license.entity.LicenseType;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class AddonsResourceGetAddonTest
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

    private Class<? extends RestLimitedAddon> expectedEntityClass;

    private final boolean isSystemAdmin;

    private final boolean isPluginEnabled;

    public AddonsResourceGetAddonTest(Class<? extends RestLimitedAddon> expectedEntityClass,
                                      boolean isSystemAdmin, boolean isPluginEnabled) {

        MockitoAnnotations.initMocks(this);
        this.expectedEntityClass = expectedEntityClass;
        this.isSystemAdmin = isSystemAdmin;
        this.isPluginEnabled = isPluginEnabled;
    }

    @Before
    public void setup()
    {
        this.resource = new AddonsResource(this.addonRegistry, this.licenseRetriever, this.connectApplinkManager,
                this.connectAddonManager, this.connectAddOnInstaller, this.applicationProperties, this.userManager,
                this.productAccessor);
    }

    @Parameterized.Parameters
    public static Collection entityClassByClientType()
    {
        return Arrays.asList(new Object[][]{
                {RestInternalAddon.class, true, true},
                {RestAddon.class, false, true},
                {RestLimitedAddon.class, false, false}
        });
    }

    @Test
    public void shouldReturnCorrectAddonRepresentation()
    {
        String key = "my-addon-key";
        String version = "0.1";
        PluginState state = isPluginEnabled ? PluginState.INSTALLED : PluginState.DISABLED;
        String productName = Product.JIRA.getName();
        LicenseStatus licenseStatus = LicenseStatus.ACTIVE;
        LicenseType licenseType = LicenseType.DEVELOPER;
        boolean isEvaluationLicense = false;
        String contactName = "Charlie Atlassian";
        String contactEmail = "charlie@atlassian.com";
        String supportEntitlementNumber = "abc123";

        UserKey userKey = new UserKey("charlie");
        when(userManager.getRemoteUserKey()).thenReturn(userKey);
        when(userManager.isSystemAdmin(userKey)).thenReturn(isSystemAdmin);

        ConnectAddonBean beanMock = mock(ConnectAddonBean.class);
        when(beanMock.getKey()).thenReturn(key);
        when(beanMock.getVersion()).thenReturn(version);

        Contact contactMock = mock(Contact.class);
        when(contactMock.getName()).thenReturn(contactName);
        when(contactMock.getEmail()).thenReturn(contactEmail);

        ProductLicense productLicenseMock = mock(ProductLicense.class);
        when(productLicenseMock.getContacts()).thenReturn(Lists.newArrayList(contactMock));

        PluginLicense licenseMock = mock(PluginLicense.class);
        when(licenseMock.isActive()).thenReturn(isPluginEnabled);
        when(licenseMock.getLicenseType()).thenReturn(licenseType);
        when(licenseMock.isEvaluation()).thenReturn(isEvaluationLicense);
        when(licenseMock.getSupportEntitlementNumber()).thenReturn(com.atlassian.upm.api.util.Option.some(supportEntitlementNumber));

        when(addonRegistry.getAddonBean(key)).thenReturn(Option.some(beanMock));
        when(addonRegistry.getRestartState(key)).thenReturn(state);
        when(productAccessor.getProductLicense()).thenReturn(Option.option(productLicenseMock));
        when(applicationProperties.getDisplayName()).thenReturn(productName);
        when(licenseRetriever.getLicense(key)).thenReturn(com.atlassian.upm.api.util.Option.some(licenseMock));
        when(applicationProperties.getBaseUrl(UrlMode.CANONICAL)).thenReturn("http://localhost:2990/jira");

        Response response = resource.getAddon(key);
        Object entity = response.getEntity();
        RestLimitedAddon limitedAddon = expectedEntityClass.cast(entity);

        assertThat(limitedAddon.getKey(), equalTo(key));
        assertThat(limitedAddon.getVersion(), equalTo(version));
        assertThat(limitedAddon.getState(), equalTo(state.name()));

        if (RestAddon.class.isAssignableFrom(expectedEntityClass)) {
            RestAddon addon = (RestAddon)limitedAddon;

            RestHost host = addon.getHost();
            assertThat(host.getProduct(), equalTo(productName));
            assertThat(host.getContacts(), hasSize(1));
            assertThat(host.getContacts().get(0).getName(), equalTo(contactName));
            assertThat(host.getContacts().get(0).getEmail(), equalTo(contactEmail));

            RestAddonLicense license = addon.getLicense();
            assertThat(license.getStatus(), equalTo(licenseStatus));
            assertThat(license.getType(), equalTo(licenseType));
            assertThat(license.isEvaluation(), equalTo(isEvaluationLicense));
            assertThat(license.getSupportEntitlementNumber(), equalTo(supportEntitlementNumber));

            assertThat(addon.getLinks(), instanceOf(RestRelatedLinks.class));

            if (RestInternalAddon.class.isAssignableFrom(expectedEntityClass))
            {
                RestInternalAddon internalAddon = (RestInternalAddon) limitedAddon;

                assertThat(internalAddon.getApplink(), nullValue());
            }
        }
    }
}
