package com.atlassian.plugin.connect.plugin.web.iframe;

import com.atlassian.html.encode.JavascriptEncoder;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameContext;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderer;
import com.atlassian.plugin.connect.plugin.lifecycle.upm.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.web.HostApplicationInfo;
import com.atlassian.sal.api.timezone.TimeZoneManager;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import org.apache.commons.lang.ObjectUtils;
import org.json.simple.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.web.iframe.EncodingUtils.escapeQuotes;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

@Named
public final class IFrameRendererImpl implements IFrameRenderer {
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final HostApplicationInfo hostApplicationInfo;
    private final TemplateRenderer templateRenderer;
    private final TimeZoneManager timeZoneManager;
    private final LicenseRetriever licenseRetriever;
    private final LocaleHelper localeHelper;
    private final UserManager userManager;

    @Inject
    public IFrameRendererImpl(TemplateRenderer templateRenderer,
                              HostApplicationInfo hostApplicationInfo,
                              RemotablePluginAccessorFactory remotablePluginAccessorFactory,
                              TimeZoneManager timeZoneManager,
                              final LicenseRetriever licenseRetriever,
                              LocaleHelper localeHelper, UserManager userManager) {
        this.licenseRetriever = licenseRetriever;
        this.localeHelper = localeHelper;
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
        this.timeZoneManager = timeZoneManager;
        this.templateRenderer = checkNotNull(templateRenderer);
        this.hostApplicationInfo = checkNotNull(hostApplicationInfo);
        this.userManager = userManager;
    }

    @Override
    public String render(IFrameContext iframeContext, String remoteUser) throws IOException {
        return render(iframeContext, "", Collections.<String, String[]>emptyMap(), Collections.<String, Object>emptyMap());
    }

    @Override
    public String render(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, Map<String, Object> productContext) throws IOException {
        return renderWithTemplate(prepareContext(iframeContext, extraPath, queryParams, productContext), "velocity/deprecated/iframe-body.vm");
    }

    @Override
    public String renderInline(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, Map<String, Object> productContext) throws IOException {
        return renderWithTemplate(prepareContext(iframeContext, extraPath, queryParams, productContext), "velocity/deprecated/iframe-body-inline.vm");
    }

    private String renderWithTemplate(Map<String, Object> ctx, String templatePath) throws IOException {
        StringWriter output = new StringWriter();
        templateRenderer.render(templatePath, ctx, output);
        return output.toString();
    }

    private Map<String, Object> prepareContext(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, Map<String, Object> productContext)
            throws IOException {
        RemotablePluginAccessor remotablePluginAccessor = remotablePluginAccessorFactory.get(iframeContext.getPluginKey());

        final URI hostUrl = hostApplicationInfo.getUrl();

        UriBuilder uriBuilder = new UriBuilder(Uri.parse(iframeContext.getIframePath()));
        uriBuilder.setPath(uriBuilder.getPath() + ObjectUtils.toString(extraPath));
        final URI iframeUrl = uriBuilder.toUri().toJavaUri();

        String[] dialog = queryParams.get("dialog");
        final String timeZone = timeZoneManager.getUserTimeZone().getID();
        UserProfile user = userManager.getRemoteUser();
        String userName = user == null ? "" : user.getUsername();
        String userKey = user == null ? "" : user.getUserKey().getStringValue();

        Map<String, String[]> allParams = newHashMap(queryParams);
        allParams.put("user_id", new String[]{userName});
        allParams.put("user_key", new String[]{userKey});
        allParams.put("xdm_e", new String[]{hostUrl.toString()});
        allParams.put("xdm_c", new String[]{"channel-" + iframeContext.getNamespace()});
        allParams.put("cp", new String[]{hostApplicationInfo.getContextPath()});
        allParams.put("tz", new String[]{timeZone});
        allParams.put("loc", new String[]{localeHelper.getLocaleTag()});
        allParams.put("lic", new String[]{licenseRetriever.getLicenseStatus(iframeContext.getPluginKey()).value()});

        if (dialog != null && dialog.length == 1) {
            allParams.put("dialog", dialog);
        }
        String signedUrl = remotablePluginAccessor.signGetUrl(iframeUrl, allParams);

        // clear xdm params as they are added by easyxdm later
        signedUrl = new UriBuilder(Uri.parse(signedUrl))
                .toString();

        Map<String, Object> ctx = newHashMap(iframeContext.getIFrameParams().getAsMap());
        ctx.put("iframeSrcHtml", escapeQuotes(signedUrl));
        ctx.put("plugin", remotablePluginAccessor);
        ctx.put("namespace", iframeContext.getNamespace());
        ctx.put("contextPath", hostApplicationInfo.getContextPath());

        ctx.put("userId", userName);
        ctx.put("userKey", userKey);

        ctx.put("timeZone", timeZone);

        if (dialog != null && dialog.length == 1) {
            ctx.put("dialog", dialog[0]);
        }

        String[] simpleDialog = queryParams.get("simpleDialog");
        if (simpleDialog != null && simpleDialog.length == 1) {
            ctx.put("simpleDialog", simpleDialog[0]);
        }

        ctx.put("productContextHtml", encodeProductContext(productContext));
        return ctx;
    }

    private String encodeProductContext(Map<String, Object> productContext) throws IOException {
        JSONObject jsonObj = new JSONObject();
        for (Map.Entry<String, Object> entry : productContext.entrySet()) {
            Object value = entry.getValue();

            // json-simple doesn't support serializing java arrays - so unwrap or convert to java.util.List
            if (value instanceof Object[]) {
                Object[] objArray = (Object[]) value;
                switch (objArray.length) {
                    case 0:
                        value = null;
                        break;
                    case 1:
                        value = objArray[0];
                        break;
                    default:
                        value = Arrays.asList((Object[]) entry.getValue());
                }
            }

            jsonObj.put(entry.getKey(), value);
        }

        StringWriter writer = new StringWriter();
        JavascriptEncoder.escape(writer, jsonObj.toJSONString());
        return writer.toString();
    }
}
