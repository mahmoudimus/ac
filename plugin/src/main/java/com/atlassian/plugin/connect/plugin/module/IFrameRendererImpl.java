package com.atlassian.plugin.connect.plugin.module;

import com.atlassian.html.encode.JavascriptEncoder;
import com.atlassian.plugin.connect.plugin.UserPreferencesRetriever;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.plugin.util.LocaleHelper;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.ObjectUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.util.EncodingUtils.escapeQuotes;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

@Component
public final class IFrameRendererImpl implements IFrameRenderer
{
    private final TemplateRenderer templateRenderer;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final IFrameHost iframeHost;
    private final LicenseRetriever licenseRetriever;
    private final LocaleHelper localeHelper;
    private final UserPreferencesRetriever userPreferencesRetriever;
    private final UserManager userManager;

    @Autowired
    public IFrameRendererImpl(TemplateRenderer templateRenderer,
            IFrameHost iframeHost,
            RemotablePluginAccessorFactory remotablePluginAccessorFactory,
            UserPreferencesRetriever userPreferencesRetriever, final LicenseRetriever licenseRetriever,
            LocaleHelper localeHelper, UserManager userManager)
    {
        this.licenseRetriever = licenseRetriever;
        this.localeHelper = localeHelper;
        this.userPreferencesRetriever = checkNotNull(userPreferencesRetriever);
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
        this.templateRenderer = checkNotNull(templateRenderer);
        this.iframeHost = checkNotNull(iframeHost);
        this.userManager = userManager;
    }

    @Override
    public String render(IFrameContext iframeContext, String remoteUser) throws IOException
    {
        return render(iframeContext, "", Collections.<String, String[]>emptyMap(), remoteUser, Collections.<String, Object>emptyMap());
    }

    @Override
    @Deprecated
    public String render(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser) throws IOException
    {
        return render(iframeContext, extraPath, queryParams, remoteUser, Collections.<String, Object>emptyMap());
    }

    @Override
    public String render(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser, Map<String, Object> productContext) throws IOException
    {
        return renderWithTemplate(prepareContext(iframeContext, extraPath, queryParams, remoteUser, productContext), "velocity/iframe-body.vm");
    }

    @Override
    @Deprecated
    public String renderInline(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser) throws IOException
    {
        return renderInline(iframeContext, extraPath, queryParams, remoteUser, Collections.<String, Object>emptyMap());
    }

    @Override
    public String renderInline(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser, Map<String, Object> productContext) throws IOException
    {
        return renderWithTemplate(prepareContext(iframeContext, extraPath, queryParams, remoteUser, productContext), "velocity/iframe-body-inline.vm");
    }

    private String renderWithTemplate(Map<String, Object> ctx, String templatePath) throws IOException
    {
        StringWriter output = new StringWriter();
        templateRenderer.render(templatePath, ctx, output);
        return output.toString();
    }

    private Map<String, Object> prepareContext(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser, Map<String, Object> productContext)
            throws IOException
    {
        RemotablePluginAccessor remotablePluginAccessor = remotablePluginAccessorFactory.get(iframeContext.getPluginKey());

        final URI hostUrl = iframeHost.getUrl();

        UriBuilder uriBuilder = new UriBuilder(Uri.parse(iframeContext.getIframePath()));
        uriBuilder.setPath(uriBuilder.getPath() + ObjectUtils.toString(extraPath));
        final URI iframeUrl = uriBuilder.toUri().toJavaUri();

        String[] dialog = queryParams.get("dialog");
        final String timeZone = userPreferencesRetriever.getTimeZoneFor(remoteUser).getID();
        UserProfile user = userManager.getUserProfile(remoteUser);

        Map<String,String[]> allParams = newHashMap(queryParams);
        allParams.put("user_id", new String[]{remoteUser});
        allParams.put("user_key", new String[] {user == null ? "" : user.getUserKey().getStringValue()});
        allParams.put("xdm_e", new String[]{hostUrl.toString()});
        allParams.put("xdm_c", new String[]{"channel-" + iframeContext.getNamespace()});
        allParams.put("xdm_p", new String[]{"1"});
        allParams.put("cp", new String[]{iframeHost.getContextPath()});
        allParams.put("tz", new String[]{timeZone});
        allParams.put("loc", new String[]{localeHelper.getLocaleTag()});
        allParams.put("lic", new String[]{licenseRetriever.getLicenseStatus(iframeContext.getPluginKey()).value()});

        if (dialog != null && dialog.length == 1) allParams.put("dialog", dialog);
        String signedUrl = remotablePluginAccessor.signGetUrl(iframeUrl, allParams);

        // clear xdm params as they are added by easyxdm later
        signedUrl = new UriBuilder(Uri.parse(signedUrl))
                .removeQueryParameter("xdm_e")
                .removeQueryParameter("xdm_c")
                .removeQueryParameter("xdm_p")
                .toString();

        Map<String,Object> ctx = newHashMap(iframeContext.getIFrameParams().getAsMap());
        ctx.put("iframeSrcHtml", escapeQuotes(signedUrl));
        ctx.put("plugin", remotablePluginAccessor);
        ctx.put("namespace", iframeContext.getNamespace());
        ctx.put("contextPath", iframeHost.getContextPath());

        ctx.put("userId", remoteUser == null ? "" : remoteUser);
        ctx.put("userKey", user == null ? "" : user.getUserKey().getStringValue());

        ctx.put("data", ImmutableMap.of("timeZone", timeZone));
        if (dialog != null && dialog.length == 1) ctx.put("dialog", dialog[0]);

        String[] simpleDialog = queryParams.get("simpleDialog");
        if (simpleDialog != null && simpleDialog.length == 1) ctx.put("simpleDialog", simpleDialog[0]);

        ctx.put("productContextHtml", encodeProductContext(productContext));
        return ctx;
    }

    private String encodeProductContext(Map<String, Object> productContext) throws IOException
    {
        String json = new JSONObject(productContext).toString();
        StringWriter writer = new StringWriter();
        JavascriptEncoder.escape(writer, json);
        return writer.toString();
    }
}
