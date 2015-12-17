package com.atlassian.plugin.connect.confluence.blueprint;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.atlassian.confluence.api.service.content.ContentBodyConversionService;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.util.concurrent.Promise;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnit44Runner;

import static com.atlassian.plugin.connect.api.request.HttpMethod.POST;
import static com.atlassian.plugin.connect.confluence.blueprint.BlueprintContextProvider.CONTENT_TEMPLATE_KEY;
import static com.atlassian.plugin.connect.confluence.blueprint.BlueprintContextProvider.CONTEXT_URL_KEY;
import static com.atlassian.plugin.connect.confluence.blueprint.BlueprintContextProvider.REMOTE_ADDON_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnit44Runner.class)
public class BlueprintContextProviderTest
{
    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @Mock private EventPublisher eventPublisher;
    @Mock private RemotablePluginAccessorFactory accessorFactory;
    @Mock private ContentBodyConversionService converter;
    @Mock private UserManager userManager;
    @Mock private RemotablePluginAccessor mockAccessor;

    @Mock private Promise<String> mockPromise;
    @Mock private BlueprintContext mockContextObj;

    private BlueprintContextProvider blueprintContextProvider;

    @Before
    public void setUp() throws Exception
    {
        blueprintContextProvider = new BlueprintContextProvider(accessorFactory, converter, userManager, eventPublisher);
        when(converter.convert(any(), any())).thenAnswer(a -> a.getArguments()[0]);
        when(accessorFactory.getOrThrow("remote-addon-key")).thenReturn(mockAccessor);
        when(mockAccessor.executeAsync(eq(POST), eq(URI.create("/context-url")), anyMap(), anyMap(), any())).thenReturn(mockPromise);
        mockResponse(mockPromise, "testfiles/blueprint/context-response.json");

        blueprintContextProvider.init(ImmutableMap.of(
                CONTEXT_URL_KEY, "/context-url",
                CONTENT_TEMPLATE_KEY, "blueprint-template-key",
                REMOTE_ADDON_KEY, "remote-addon-key"
        ));
    }

    private static void mockResponse(final Promise<String> mockPromise, final String resourceName) throws Exception
    {
        InputStream resourceAsStream = BlueprintContextProviderTest.class.getClassLoader().getResourceAsStream(resourceName);
        String response =  IOUtils.toString(resourceAsStream);
        when(mockPromise.get(anyLong(), eq(TimeUnit.SECONDS))).thenReturn(response);
    }

    private static void mockResponseException(final Promise<String> mockPromise, final Exception e) throws Exception
    {
        when(mockPromise.get(anyLong(), eq(TimeUnit.SECONDS))).thenAnswer(a -> {
            throw e;
        });
    }

    @Test
    public void testBlueprintContextUpdateFailsWhenTimeout() throws Exception
    {
        mockResponseException(mockPromise, new TimeoutException("not a real exception"));
        //we are using RuntimeException (pending addition of a new blueprint specific exception).
        exceptions.expect(RuntimeException.class);
        blueprintContextProvider.updateBlueprintContext(mockContextObj);
    }
    @Test
    public void testBlueprintContextUpdateFailsWhenExecutionInterrupted() throws Exception
    {
        mockResponseException(mockPromise, new InterruptedException("not a real exception"));
        //we are using RuntimeException (pending addition of a new blueprint specific exception).
        exceptions.expect(RuntimeException.class);
        blueprintContextProvider.updateBlueprintContext(mockContextObj);
    }

    @Test
    public void testBlueprintContextUpdateFailsWhenExecutionHasError() throws Exception
    {
        mockResponseException(mockPromise, new ExecutionException("not a real exception", new Throwable()));
        //we are using RuntimeException (pending addition of a new blueprint specific exception).
        exceptions.expect(RuntimeException.class);
        blueprintContextProvider.updateBlueprintContext(mockContextObj);
    }

    @Test
    public void testBlueprintContextIsUpdatedCorrectlyWithResponse()
    {
        blueprintContextProvider.updateBlueprintContext(mockContextObj);
        verify(mockContextObj).put("ContentPageTitle", "test page title for something");
        verify(mockContextObj).put("custom1", "something 1");
        verify(mockContextObj).put("custom2", "something 2");
        verify(mockContextObj).put("custom3", "something 3");
        verify(mockContextObj).put("custom4", "<ac:structured-macro ac:name=\"cheese\" ac:schema-version=\"1\"/> ");
    }

    @Test
    public void testInitSuccessful()
    {
        assertThat(blueprintContextProvider.getAddonKey(), is("remote-addon-key"));
        assertThat(blueprintContextProvider.getBlueprintKey(), is("blueprint-template-key"));
        assertThat(blueprintContextProvider.getContextUrl(), is("/context-url"));
    }

    @Test(expected = IllegalStateException.class)
    public void testInitFailsWhenAddonKeyDoesNotExist()
    {
        when(accessorFactory.getOrThrow("remote-addon-key")).thenAnswer(a -> {
            throw new IllegalStateException();
        });
        blueprintContextProvider.init(ImmutableMap.of(
                CONTEXT_URL_KEY, "/context-url",
                CONTENT_TEMPLATE_KEY, "blueprint-template-key",
                REMOTE_ADDON_KEY, "remote-addon-key"
        ));
    }

    @Test
    public void testInitFailsWhenMissingContextUrl()
    {
        exceptions.expect(PluginParseException.class);
        exceptions.expectMessage("the context-url is not specified. "
                                 + "The context-provider element should not have been supplied "
                                 + "if the connect addon blueprint module did not provide a context provider url");
        blueprintContextProvider.init(ImmutableMap.of(
                CONTENT_TEMPLATE_KEY, "blueprint-template-key",
                REMOTE_ADDON_KEY, "no-such-addon-key"
        ));
    }

    @Test
    public void testInitFailsWhenMissingTemplateKey() throws Exception
    {
        exceptions.expect(PluginParseException.class);
        exceptions.expectMessage("the content-template-key is not specified");
        blueprintContextProvider.init(ImmutableMap.of(
                CONTEXT_URL_KEY, "/context-url",
                REMOTE_ADDON_KEY, "no-such-addon-key"
        ));

    }

    @Test
    public void testInitFailsWhenMissingAddonKey() throws Exception
    {
        exceptions.expect(PluginParseException.class);
        exceptions.expectMessage("the addon-key is not specified.");
        blueprintContextProvider.init(ImmutableMap.of(
                CONTEXT_URL_KEY, "/context-url",
                CONTENT_TEMPLATE_KEY, "blueprint-template-key"
        ));
    }
}
