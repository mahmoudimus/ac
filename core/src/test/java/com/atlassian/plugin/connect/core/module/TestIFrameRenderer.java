package com.atlassian.plugin.connect.core.module;

import com.atlassian.plugin.connect.core.license.LicenseRetriever;
import com.atlassian.plugin.connect.core.license.LicenseStatus;
import com.atlassian.plugin.connect.core.util.LocaleHelper;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.connect.spi.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.spi.user.UserPreferencesRetriever;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.SimpleTimeZone;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestIFrameRenderer
{
    @Mock private TemplateRenderer templateRenderer;
    @Mock private RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    @Mock private RemotablePluginAccessor remotePluginAccessor;
    @Mock private HostApplicationInfo hostApplicationInfo;
    @Mock private LicenseRetriever licenseRetriever;
    @Mock private LocaleHelper localeHelper;
    @Mock private UserPreferencesRetriever userPreferencesRetriever;
    @Mock private UserManager userManager;

    private IFrameRenderer iframeRenderer;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        this.iframeRenderer = new IFrameRendererImpl(templateRenderer, hostApplicationInfo, remotablePluginAccessorFactory,
                userPreferencesRetriever, licenseRetriever, localeHelper, userManager);
    }

    @Test
    public void testRenderTemplatePath() throws Exception
    {
        mockAllTheThings("jim", "my-timezone", "my-context-path", "my-url", "a.b", "my-signed-url");
        iframeRenderer.render(createContext("a.b", "my-path", "my-namespace"), "", emptyParams(), "jim", emptyContext());

        String path = getActualTemplateRendererPath();
        assertEquals("velocity/deprecated/iframe-body.vm", path);
    }

    @Test
    public void testRenderInlineTemplatePath() throws Exception
    {
        mockAllTheThings("jim", "my-timezone", "my-context-path", "my-url", "a.b", "my-signed-url");
        iframeRenderer.renderInline(createContext("a.b", "my-path", "my-namespace"), "", emptyParams(), "jim", emptyContext());

        String path = getActualTemplateRendererPath();
        assertEquals("velocity/deprecated/iframe-body-inline.vm", path);
    }

    @Test
    public void testContext() throws IOException
    {
        mockAllTheThings("jim", "my-timezone", "my-context-path", "my-url", "a.b", "my-signed-url");
        iframeRenderer.render(createContext("a.b", "my-path", "my-namespace"), "", emptyParams(), "jim", emptyContext());

        Map<String, Object> ctx = getActualTemplateRendererContext();
        assertEquals("{}", ctx.get("productContextHtml"));
        assertEquals("jim-key", ctx.get("userKey"));
        assertEquals("jim", ctx.get("userId"));
        assertEquals("my-timezone", ctx.get("timeZone"));
        assertEquals("my-context-path", ctx.get("contextPath"));
        assertEquals("my-namespace", ctx.get("namespace"));
        assertEquals("my-signed-url", ctx.get("iframeSrcHtml"));
    }

    @Test
    public void testProductContext() throws Exception
    {
        Map<String, Object> productContext = ImmutableMap.<String, Object>of(
                "hell", ImmutableMap.of("o", "world", "a", "good"),
                "good", "bye"
        );
        mockAllTheThings("jim", "my-timezone", "my-context-path", "my-url", "a.b", "my-signed-url");
        iframeRenderer.render(createContext("a.b", "my-path", "my-namespace"), "", emptyParams(), "jim", productContext);

        Map<String, Object> ctx = getActualTemplateRendererContext();
        // Need to en-encode as this is wrapped for inclusion in js
        String jsJson = (String) ctx.get("productContextHtml");
        jsJson = jsJson.replace("\\\"", "\"");
        Map<String, Object> parsedJson = (Map<String, Object>) new JSONParser().parse(jsJson);
        assertEquals(productContext, parsedJson);
    }

    @Test
    public void testQueryParams() throws Exception
    {
        Map<String, String[]> params = ImmutableMap.of(
                "hello", new String[]{ "world" },
                "hella", new String[]{ "good" }
        );
        mockAllTheThings("jim", "my-timezone", "my-context-path", "my-url", "a.b", "my-signed-url");
        iframeRenderer.render(createContext("a.b", "my-path", "my-namespace"), "", params, "jim", emptyContext());

        Map<String, String[]> signParams = getActualSignedUrlParams();
        assertArrayEquals(params.get("hello"), signParams.get("hello"));
        assertArrayEquals(params.get("hella"), signParams.get("hella"));
    }

    private void mockAllTheThings(String remoteUser, String timezone, String iframeContextPath, String iframeHostUrl,
        String pluginKey, String expectedSignedUrl)
    {
        when(userPreferencesRetriever.getTimeZoneFor(remoteUser)).thenReturn(new SimpleTimeZone(10, timezone));
        UserProfile userProfile = mock(UserProfile.class);
        when(userProfile.getUserKey()).thenReturn(new UserKey(remoteUser + "-key"));
        when(userManager.getUserProfile(remoteUser)).thenReturn(userProfile);
        when(hostApplicationInfo.getContextPath()).thenReturn(iframeContextPath);
        when(hostApplicationInfo.getUrl()).thenReturn(URI.create(iframeHostUrl));
        when(licenseRetriever.getLicenseStatus(pluginKey)).thenReturn(LicenseStatus.ACTIVE);
        when(remotablePluginAccessorFactory.get(pluginKey)).thenReturn(remotePluginAccessor);
        when(remotePluginAccessor.signGetUrl(any(URI.class), any(Map.class))).thenReturn(expectedSignedUrl);
    }

    private Map<String, String[]> getActualSignedUrlParams()
    {
        ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        verify(remotePluginAccessor).signGetUrl(any(URI.class), argument.capture());
        return argument.getValue();
    }

    private String getActualTemplateRendererPath() throws IOException
    {
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(templateRenderer).render(argument.capture(), any(Map.class), any(Writer.class));
        return argument.getValue();
    }

    private Map<String, Object> getActualTemplateRendererContext() throws IOException
    {
        ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        verify(templateRenderer).render(anyString(), argument.capture(), any(Writer.class));
        return argument.getValue();
    }

    public static final Map<String, String[]> emptyParams()
    {
        return Collections.emptyMap();
    }

    public static final Map<String, Object> emptyContext()
    {
        return Collections.emptyMap();
    }

    public static IFrameContext createContext(String pluginKey, String iframePath, String namespace)
    {
        return createContext(pluginKey, iframePath, namespace, Collections.<String, Object>emptyMap());
    }

    public static IFrameContext createContext(String pluginKey, String iframePath, String namespace, Map<String, Object> params)
    {
        final Map<String, Object> internalParams = Maps.newHashMap(params);
        IFrameParams iframeParams = new IFrameParams()
        {
            @Override
            public Map<String, Object> getAsMap()
            {
                return internalParams;
            }

            @Override
            public void setParam(String key, String value)
            {
                internalParams.put(key, value);
            }
        };
        return new IFrameContextImpl(pluginKey, iframePath, namespace, iframeParams);
    }
}
