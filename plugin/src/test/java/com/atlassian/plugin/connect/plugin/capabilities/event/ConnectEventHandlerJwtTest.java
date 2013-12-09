package com.atlassian.plugin.connect.plugin.capabilities.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.jira.security.auth.trustedapps.KeyFactory;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.plugin.capabilities.BeanToModuleRegistrar;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.installer.ConnectDescriptorRegistry;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.webhooks.spi.plugin.RequestSigner;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.net.URI;
import java.util.Dictionary;

import static com.atlassian.plugin.connect.plugin.util.ConnectInstallationTestUtil.*;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectEventHandlerJwtTest
{
    private @Mock EventPublisher eventPublisher;
    private @Mock PluginEventManager pluginEventManager;
    private @Mock UserManager userManager;
    private @Mock HttpClient httpClient;
    private @Mock RequestSigner requestSigner;
    private @Mock ConsumerService consumerService;
    private @Mock ApplicationProperties applicationProperties;
    private @Mock ProductAccessor productAccessor;
    private @Mock BundleContext bundleContext;
    private @Mock JsonConnectAddOnIdentifierService connectIdentifier;
    private @Mock ConnectDescriptorRegistry descriptorRegistry;
    private @Mock BeanToModuleRegistrar beanToModuleRegistrar;
    private @Mock LicenseRetriever licenseRetriever;

    private @Mock Request.Builder requestBuilder;
    private @Mock Bundle bundle;
    private @Mock Dictionary dictionary;
    private @Mock ResponsePromise responsePromise;
    private @Mock Response response;

    private static final String SHARED_SECRET = "shared secret";
    private static final String PUBLIC_KEY = "public key";

    @Test
    public void installPostContainsSharedSecret()
    {
        verify(requestBuilder).setEntity(argThat(hasSharedSecret()));
    }

    @Test
    public void installPostContainsValidSharedSecret()
    {
        verify(requestBuilder).setEntity(argThat(hasValidSharedSecret()));
    }

    @Test
    public void installUrlIsPosted()
    {
        verify(requestBuilder).execute(Request.Method.POST);
    }

    private static ArgumentMatcher<String> hasValidSharedSecret()
    {
        return new ArgumentMatcher<String>()
        {
            @Override
            public boolean matches(Object actual)
            {
                return actual instanceof String
                        && !StringUtils.isEmpty((String)actual)
                        && SHARED_SECRET.equals(new JsonParser().parse((String)actual).getAsJsonObject().get(SHARED_SECRET_FIELD_NAME).getAsString());
            }
        };
    }

    @Before
    public void beforeEachTest()
    {
        when(httpClient.newRequest(Matchers.<URI>any())).thenReturn(requestBuilder);
        when(bundleContext.getBundle()).thenReturn(bundle);
        when(bundle.getHeaders()).thenReturn(dictionary);
        when(consumerService.getConsumer()).thenReturn(Consumer.key("whatever").name("whatever").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).publicKey(new KeyFactory.InvalidPublicKey(new Exception())).build());
        when(requestBuilder.execute(Request.Method.POST)).thenReturn(responsePromise);
        when(responsePromise.claim()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);
        ConnectEventHandler connectEventHandler = new ConnectEventHandler(eventPublisher, pluginEventManager, userManager, httpClient, requestSigner, consumerService,
                applicationProperties, productAccessor, bundleContext, connectIdentifier, descriptorRegistry, beanToModuleRegistrar, licenseRetriever);
        connectEventHandler.pluginInstalled(createBean(AuthenticationType.JWT, PUBLIC_KEY, "/baseUrl"), SHARED_SECRET);
    }
}
