package com.atlassian.plugin.connect.plugin.module.webitem;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.descriptors.WebFragmentModuleDescriptor;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext.addon;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext.product;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteWebLinkTest
{
    private static final String MY_KEY = "MyKey";
    private static final String MY_PROJECT = "MyProj";
    private static final ImmutableMap<String, Object> CONTEXT = ImmutableMap.of("project.id", (Object) MY_PROJECT);
    private static final String URL = "/foo?bar=${project.id}";
    private static final String SUBSTITUTED_URL = "/foo?bar=" + MY_PROJECT;
    private static final String HOST_CONTEXT = "hostContext";
    private static final String ADDON_BASEURL = "http://myaddon.com";
    private static final String SIGNED_ADDON_URL = ADDON_BASEURL + SUBSTITUTED_URL + "&signOnTheDottedLine";


    @Mock
    private WebFragmentModuleDescriptor webFragmentModuleDescriptor;
    @Mock
    private WebFragmentHelper webFragmentHelper;
    @Mock
    private UrlVariableSubstitutor urlVariableSubstitutor;
    @Mock
    private ContextMapURLSerializer urlParametersSerializer;
    @Mock
    private HttpServletRequest servletRequest;
    @Mock
    private RemotablePluginAccessor remotablePluginAccessor;

    @Before
    public void init()
    {
        when(urlParametersSerializer.getExtractedWebPanelParameters(anyMap())).thenReturn(CONTEXT);
        when(urlVariableSubstitutor.replace(anyString(), anyMap())).thenReturn(SUBSTITUTED_URL);
        when(servletRequest.getContextPath()).thenReturn(HOST_CONTEXT);
        when(remotablePluginAccessor.signGetUrl(any(URI.class), anyMap())).thenReturn(SIGNED_ADDON_URL);
    }


    @Test
    public void productUrlsRelativeToHost()
    {
        assertThat(getDisplayableUrl(product, false), equalTo(HOST_CONTEXT + SUBSTITUTED_URL));
    }

    @Test
    public void absoluteUrlsUnmolested()
    {
        assertThat(getDisplayableUrl(product, true), equalTo(SUBSTITUTED_URL));
        assertThat(getDisplayableUrl(addon, true), equalTo(SUBSTITUTED_URL));
        assertThat(getDisplayableUrl(null, true), equalTo(SUBSTITUTED_URL));
    }

    @Test
    public void addonUrlsSignedAndRelativeToAddonServer()
    {
        assertThat(getDisplayableUrl(addon, false), equalTo(SIGNED_ADDON_URL));
    }

    private String getDisplayableUrl(AddOnUrlContext urlContext, boolean absolute)
    {
        return new RemoteWebLink(webFragmentModuleDescriptor, webFragmentHelper, urlVariableSubstitutor, urlParametersSerializer,
                remotablePluginAccessor, URL, MY_KEY, absolute, urlContext).getDisplayableUrl(servletRequest, CONTEXT);
    }
}
