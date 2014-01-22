package com.atlassian.plugin.connect.plugin.module.webitem;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.WebPanelModuleContextExtractor;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.descriptors.WebFragmentModuleDescriptor;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.modules.beans.AddOnUrlContext.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteWebLinkTest
{
    private static final String PLUGIN_KEY = "my-plug";
    private static final String MODULE_KEY = "my-module";
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
    private HttpServletRequest servletRequest;

    @Mock
    private IFrameUriBuilderFactory iFrameUriBuilderFactory;

    @Mock
    private WebPanelModuleContextExtractor webPanelModuleContextExtractor;

    @Mock
    private ModuleContextFilter moduleContextFilter;

    @Before
    public void init()
    {
        when(servletRequest.getContextPath()).thenReturn(HOST_CONTEXT);
    }


    @Test
    public void productUrlsRelativeToHost()
    {
        // TODO: need to distinguish product vs decorated. Or maybe that happens further up the chain
        assertThat(getDisplayableUrl(product, false), equalTo(HOST_CONTEXT + SUBSTITUTED_URL));
    }

    @Test
    public void absoluteUrlsUnmolested()
    {
        assertThat(getDisplayableUrl(product, true), equalTo(SUBSTITUTED_URL));
        assertThat(getDisplayableUrl(decorated, true), equalTo(SUBSTITUTED_URL));
        assertThat(getDisplayableUrl(null, true), equalTo(SUBSTITUTED_URL));
    }

    @Test
    public void addonDirectUrlsSignedAndRelativeToAddonServer()
    {
        assertThat(getDisplayableUrl(addon, false), equalTo(SIGNED_ADDON_URL));
    }

    private String getDisplayableUrl(AddOnUrlContext urlContext, boolean absolute)
    {
        return new RemoteWebLink(
                webFragmentModuleDescriptor,
                webFragmentHelper,
                iFrameUriBuilderFactory,
                webPanelModuleContextExtractor,
                moduleContextFilter,
                URL,
                PLUGIN_KEY,
                MODULE_KEY,
                absolute, urlContext).getDisplayableUrl(servletRequest, CONTEXT);
    }
}
