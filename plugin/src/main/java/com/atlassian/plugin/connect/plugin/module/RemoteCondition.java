package com.atlassian.plugin.connect.plugin.module;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.util.BundleUtil;
import com.atlassian.plugin.connect.plugin.util.LocaleHelper;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.event.AddOnConditionFailedEvent;
import com.atlassian.plugin.connect.spi.event.AddOnConditionInvokedEvent;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Function;

import org.apache.commons.lang3.time.StopWatch;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * Condition that consumes a url to determine if the item should be shown or not
 */
public final class RemoteCondition implements Condition
{
    private URI url;
    private String pluginKey;
    private String toHideSelector;
    private Iterable<String> contextParams;
    private final ProductAccessor productAccessor;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final UserManager userManager;
    private final TemplateRenderer templateRenderer;
    private final LicenseRetriever licenseRetriever;
    private final LocaleHelper localeHelper;
    private final EventPublisher eventPublisher;
    private final BundleContext bundleContext;

    private static final Logger log = LoggerFactory.getLogger(RemoteCondition.class);

    public RemoteCondition(ProductAccessor productAccessor,
                           RemotablePluginAccessorFactory remotablePluginAccessorFactory,
                           UserManager userManager,
                           TemplateRenderer templateRenderer,
                           LicenseRetriever licenseRetriever,
                           LocaleHelper localeHelper,
                           EventPublisher eventPublisher,
                           BundleContext bundleContext)
    {
        this.productAccessor = productAccessor;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.userManager = userManager;
        this.templateRenderer = templateRenderer;
        this.licenseRetriever = licenseRetriever;
        this.localeHelper = localeHelper;
        this.eventPublisher = eventPublisher;
        this.bundleContext = bundleContext;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        url = URI.create(params.get("url"));
        pluginKey = params.get("pluginKey");
        toHideSelector = params.get("toHideSelector");
        contextParams = emptyList();
        if (params.get("contextParams") != null)
        {
            contextParams = asList(params.get("contextParams").split(","));
        }
        checkNotNull(url);
        checkNotNull(pluginKey);
        checkNotNull(toHideSelector);
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final String version = BundleUtil.getBundleVersion(bundleContext);
        final Map<String,String> httpHeaders = Collections.singletonMap("Atlassian-Connect-Version", version);
        return remotablePluginAccessorFactory.get(pluginKey)
                .executeAsync(HttpMethod.GET, url, getParameters(context), httpHeaders)
                .fold(
                        new Function<Throwable, Boolean>()
                        {
                            @Override
                            public Boolean apply(Throwable t)
                            {
                                final long elapsedMillisecs = stopWatch.getTime();
                                final String message = String.format("Unable to retrieve remote condition from addon %1$s: %2$s", pluginKey, t.getMessage());
                                log.warn("Unable to retrieve remote condition from plugin {}: {}", pluginKey, t);
                                t.printStackTrace();
                                //return "<script>AJS.log('Unable to retrieve remote condition from plugin \'" + pluginKey + "\'');</script>";
                                eventPublisher.publish(new AddOnConditionFailedEvent(pluginKey, url.getPath(), elapsedMillisecs, message));
                                return false;
                            }
                        },
                        new Function<String, Boolean>()
                        {
                            @Override
                            public Boolean apply(String value)
                            {
                                final long elapsedMillisecs = stopWatch.getTime();
                                try
                                {
                                    JSONObject obj = (JSONObject) JSONValue.parseWithException(value);
//                                                                                            if ((Boolean) obj.get("shouldDisplay"))
//                                                                                            {
//                                                                                                return "<script>AJS.$(\"" + toHideSelector + "\").removeClass('hidden').parent().removeClass('hidden');</script>";
//                                                                                            }
                                    eventPublisher.publish(new AddOnConditionInvokedEvent(pluginKey, url.getPath(), elapsedMillisecs));
                                    return (Boolean) obj.get("shouldDisplay");
                                }
                                catch (ParseException e)
                                {
                                    final String message = "Invalid JSON returned from remote condition: " + value;
                                    log.warn(message);
                                    eventPublisher.publish(new AddOnConditionFailedEvent(pluginKey, url.getPath(), elapsedMillisecs, message));
                                    return false;
                                }

                            }
                        }
                ).claim();
    }

    private Map<String, String[]> getParameters(Map<String, Object> context)
    {
        Map<String, String[]> params = newHashMap();
        for (String contextParam : contextParams)
        {
            params.put(contextParam, new String[]{templateRenderer.renderFragment(productAccessor.getLinkContextParams().get(contextParam), context)});
        }
        UserProfile remoteUser = userManager.getRemoteUser();
        if (remoteUser != null)
        {
            params.put("user_id", new String[]{remoteUser.getUsername()});
            params.put("user_key", new String[]{remoteUser.getUserKey().getStringValue()});
            params.put("lic", new String[]{getLicenseStatusAsString(pluginKey)});
            params.put("loc", new String[]{getLocale()});
        }
        return params;
    }

    private String getLicenseStatusAsString(String pluginKey)
    {
        return licenseRetriever.getLicenseStatus(pluginKey).value();
    }

    private String getLocale()
    {
        return localeHelper.getLocaleTag();
    }
}
