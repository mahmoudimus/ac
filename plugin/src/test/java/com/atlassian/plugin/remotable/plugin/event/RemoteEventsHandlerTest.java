package com.atlassian.plugin.remotable.plugin.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.remotable.plugin.product.ProductAccessor;
import com.atlassian.sal.api.ApplicationProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.security.PublicKey;
import java.util.Dictionary;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class RemoteEventsHandlerTest
{
    private RemoteEventsHandler remoteEventsHandler;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private ConsumerService consumerService;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private PluginEventManager pluginEventManager;

    @Mock
    private BundleContext bundleContext;

    @Mock
    private ProductAccessor productAccessor;

    @Before
    public void setUp()
    {
        final Bundle bundle = mock(Bundle.class);
        final Consumer consumer = newConsumerKey();
        when(consumerService.getConsumer()).thenReturn(consumer);
        when(bundleContext.getBundle()).thenReturn(bundle);
        when(bundle.getHeaders()).thenReturn(mock(Dictionary.class));

        remoteEventsHandler = new RemoteEventsHandler(eventPublisher, consumerService, applicationProperties, productAccessor, bundleContext, pluginEventManager);
    }

    @Test
    public void testNewRemotePluginEventDataIsNullSafe()
    {
        remoteEventsHandler.newRemotePluginEventData();
    }

    private Consumer newConsumerKey()
    {
        final PublicKey publicKey = mock(PublicKey.class);
        when(publicKey.getEncoded()).thenReturn(new byte[] {});
        return Consumer.key("key").name("name").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).publicKey(publicKey).build();
    }
}
