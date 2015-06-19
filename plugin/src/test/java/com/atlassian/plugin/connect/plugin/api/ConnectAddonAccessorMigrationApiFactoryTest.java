package com.atlassian.plugin.connect.plugin.api;

import com.atlassian.plugin.connect.api.ConnectAddonAccessorMigrationApi;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

import java.util.Hashtable;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectAddonAccessorMigrationApiFactoryTest
{
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ConnectAddonAccessorMigrationApiFactory migrationApiFactory = new ConnectAddonAccessorMigrationApiFactory(mock(LicenseRetriever.class));

    @Test
    public void addOnAccessorMigrationApiFactoryProducesBeanOnlyForTempo()
    {
        Bundle bundle = mockBundle(ConnectAddonAccessorMigrationApiFactory.TEMPO_PLUGIN_KEY);

        Object service = migrationApiFactory.getService(bundle, mock(ServiceRegistration.class));

        assertThat(service, Matchers.instanceOf(ConnectAddonAccessorMigrationApi.class));
    }

    @Test
    public void addOnAccessorMigrationApiFactoryThrowsExceptionForOtherPlugins()
    {
        exception.expect(ConnectAddonAccessorMigrationApiFactory.UnauthorizedPluginException.class);

        migrationApiFactory.getService(mockBundle("bad-plugin-key"), mock(ServiceRegistration.class));
    }

    private Bundle mockBundle(final String pluginKey)
    {
        Bundle bundle = mock(Bundle.class);
        Hashtable<String, String> headers = new Hashtable<String, String>();
        headers.put(ConnectAddonAccessorMigrationApiFactory.ATLASSIAN_PLUGIN_KEY, pluginKey);
        when(bundle.getHeaders()).thenReturn(headers);
        return bundle;
    }

    private ConnectAddonAccessorMigrationApiFactory connectAddonAccessorMigrationApiFactory;
}
