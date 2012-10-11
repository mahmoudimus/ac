package com.atlassian.plugin.remotable.host.common.descriptor;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.remotable.host.common.descriptor.DescriptorUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class DisplayUrlTransformingDescriptorAccessorTest
{
    private static final String DISPLAY_URL = "this-is-a-test-display-url";
    private static final String LOCAL_BASE_URL = "a-local-base-url";

    private DescriptorAccessor descriptorAccessor;

    @Mock
    private DescriptorAccessor delegate;

    @Mock
    private LocalMountBaseUrlResolver baseUrlResolver;

    @Mock
    private DisplayUrlTransformingDescriptorAccessor.RuntimeContext runtimeContext;

    @Before
    public void setUp()
    {
        when(baseUrlResolver.getLocalMountBaseUrl(anyString())).thenReturn(LOCAL_BASE_URL);
        descriptorAccessor = new DisplayUrlTransformingDescriptorAccessor(delegate, baseUrlResolver, runtimeContext);
    }

    @Test
    public void testDisplayUrlIsNotReplaceWhenNotInDevModeForAtlassianPluginDescriptor()
    {
        testDisplayUrlIsReplaced(newAtlassianPluginDescriptor(), false, DISPLAY_URL);
    }

    @Test
    public void testDisplayUrlIsReplaceWhenInDevModeForAtlassianPluginDescriptor()
    {
        testDisplayUrlIsReplaced(newAtlassianPluginDescriptor(), true, LOCAL_BASE_URL);
    }

    private void testDisplayUrlIsReplaced(Document descriptor, boolean devMode, String displayUrl)
    {
        when(delegate.getDescriptor()).thenReturn(descriptor);
        when(runtimeContext.isDevMode()).thenReturn(devMode);
        assertEquals(displayUrl, getDisplayUrl(descriptorAccessor.getDescriptor()));
    }

    private Document newAtlassianPluginDescriptor()
    {
        final Document document = DocumentHelper.createDocument();
        final Element root = document.addElement("atlassian-plugin");
        root.addAttribute("plugins-version", "2");
        final Element remoteContainer = root.addElement("remote-plugin-container");
        addDisplayUrl(remoteContainer, DISPLAY_URL);
        return document;
    }
}
