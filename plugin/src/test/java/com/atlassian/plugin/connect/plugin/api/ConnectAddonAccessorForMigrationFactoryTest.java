package com.atlassian.plugin.connect.plugin.api;

import com.atlassian.plugin.connect.api.ConnectAddonAccessorMigrationApi;
import com.atlassian.plugin.connect.jira.api.ConnectAddonAccessorForMigrationFactory;
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

public class ConnectAddonAccessorForMigrationFactoryTest
{
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ConnectAddonAccessorForMigrationFactory migrationFactory = new ConnectAddonAccessorForMigrationFactory(mock(LicenseRetriever.class));

    @Test
    public void addOnAccessorForMigrationFactoryProducesBeanOnlyForTempo()
    {
        Bundle bundle = mockBundle(ConnectAddonAccessorForMigrationFactory.TEMPO_PLUGIN_KEY);

        Object service = migrationFactory.getService(bundle, mock(ServiceRegistration.class));

        assertThat(service, Matchers.instanceOf(ConnectAddonAccessorMigrationApi.class));
    }

    @Test
    public void addOnAccessorForMigrationFactoryThrowsExceptionForOtherPlugins()
    {
        exception.expect(ConnectAddonAccessorForMigrationFactory.UnauthorizedPluginException.class);

        migrationFactory.getService(mockBundle("bad-plugin-key"), mock(ServiceRegistration.class));
    }

    private Bundle mockBundle(final String pluginKey)
    {
        Bundle bundle = mock(Bundle.class);
        Hashtable<String, String> headers = new Hashtable<String, String>();
        headers.put(ConnectAddonAccessorForMigrationFactory.ATLASSIAN_PLUGIN_KEY, pluginKey);
        when(bundle.getHeaders()).thenReturn(headers);
        return bundle;
    }
}
