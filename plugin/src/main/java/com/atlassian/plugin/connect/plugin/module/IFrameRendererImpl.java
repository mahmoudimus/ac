package com.atlassian.plugin.connect.plugin.module;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.UserPreferencesRetriever;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.plugin.util.LocaleHelper;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.util.EncodingUtils.escapeQuotes;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

@Component
public final class IFrameRendererImpl implements IFrameRenderer
{
    private final TemplateRenderer templateRenderer;
    private final WebResourceManager webResourceManager;
    private final WebResourceUrlProvider webResourceUrlProvider;
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final IFrameHost iframeHost;
    private final Plugin acPlugin;
    private final LicenseRetriever licenseRetriever;
    private final LocaleHelper localeHelper;
    private final UserPreferencesRetriever userPreferencesRetriever;
    private final UserManager userManager;

    @Autowired
    public IFrameRendererImpl(TemplateRenderer templateRenderer,
            WebResourceManager webResourceManager,
            IFrameHost iframeHost,
            WebResourceUrlProvider webResourceUrlProvider,
            PluginRetrievalService pluginRetrievalService,
            DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory,
            UserPreferencesRetriever userPreferencesRetriever, final LicenseRetriever licenseRetriever,
            LocaleHelper localeHelper, UserManager userManager)
    {
        this.licenseRetriever = licenseRetriever;
        this.localeHelper = localeHelper;
        this.userPreferencesRetriever = checkNotNull(userPreferencesRetriever);
        this.remotablePluginAccessorFactory = checkNotNull(remotablePluginAccessorFactory);
        this.templateRenderer = checkNotNull(templateRenderer);
        this.webResourceManager = checkNotNull(webResourceManager);
        this.iframeHost = checkNotNull(iframeHost);
        this.webResourceUrlProvider = checkNotNull(webResourceUrlProvider);
        this.acPlugin = checkNotNull(pluginRetrievalService).getPlugin();
        this.userManager = userManager;
    }

    @Override
    public String render(IFrameContext iframeContext, String remoteUser) throws IOException
    {
        return render(iframeContext, "", Collections.<String, String[]>emptyMap(), remoteUser);
    }

    public void renderPage(IFrameContext iframeContext, PageInfo pageInfo, String extraPath, Map<String, String[]> queryParams, String remoteUser, Writer writer) throws IOException
    {
        try
        {
            if (!pageInfo.getCondition().shouldDisplay(Collections.<String, Object>emptyMap()))
            {
                throw new PermissionDeniedException(iframeContext.getPluginKey(), "Cannot render iframe for this page");
            }

            Map<String, Object> ctx = newHashMap(iframeContext.getIFrameParams().getAsMap());
            if (queryParams.get("width") != null)
            {
                iframeContext.getIFrameParams().setParam("width", queryParams.get("width")[0]);
            }
            if (queryParams.get("height") != null)
            {
                iframeContext.getIFrameParams().setParam("height", queryParams.get("height")[0]);
            }

			ctx.put("queryParams", contextQueryParameters(queryParams));
            ctx.put("title", pageInfo.getTitle());
            ctx.put("contextPath", iframeHost.getContextPath());
            ctx.put("iframeHtml", render(iframeContext, extraPath, queryParams, remoteUser));
            ctx.put("decorator", pageInfo.getDecorator());

			for (Map.Entry<String, String> metaTag : pageInfo.getMetaTagsContent().entrySet())
			{
				ctx.put(metaTag.getKey(), metaTag.getValue());
			}

            templateRenderer.render("velocity/iframe-page" + pageInfo.getTemplateSuffix() + ".vm", ctx, writer);
        }
        catch (PermissionDeniedException ex)
        {
            templateRenderer.render(
                    "velocity/iframe-page-accessdenied" + pageInfo.getTemplateSuffix() + ".vm",
                    ImmutableMap.<String, Object>of(
                            "title", pageInfo.getTitle(),
                            "decorator", pageInfo.getDecorator()), writer);
        }
    }

	@Override
    public String render(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser) throws IOException
    {
        Map<String, Object> ctx = prepareContext(iframeContext, extraPath, queryParams, remoteUser);

        StringWriter output = new StringWriter();
        templateRenderer.render("velocity/iframe-body.vm", ctx, output);
        return output.toString();
    }

    @Override
    public String renderInline(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser) throws IOException {
        Map<String, Object> ctx = prepareContext(iframeContext, extraPath, queryParams, remoteUser);

        StringWriter output = new StringWriter();
        templateRenderer.render("velocity/iframe-body-inline.vm", ctx, output);
        return output.toString();
    }


    private Map<String, Object> prepareContext(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser) {
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
        return ctx;
    }

    private Map<String, List<String>> contextQueryParameters(final Map<String, String[]> queryParams)
	{
		final Map<String, List<String>> ctxQueryParams = Maps.newHashMap();
		for (Map.Entry<String, String[]> param : queryParams.entrySet())
		{
			ctxQueryParams.put(param.getKey(), Arrays.asList(param.getValue()));
		}
		return ctxQueryParams;
	}
}
