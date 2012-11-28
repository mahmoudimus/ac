package com.atlassian.plugin.remotable.spi.module;

import com.atlassian.plugin.remotable.plugin.RemotablePluginAccessorFactory;
import com.atlassian.plugin.remotable.plugin.product.ProductAccessor;
import com.atlassian.plugin.remotable.plugin.util.http.bigpipe.BigPipe;
import com.atlassian.plugin.remotable.plugin.util.http.bigpipe.RequestIdAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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
public class RemoteCondition implements Condition
{

    private URI url;
    private String pluginKey;
    private String toHideSelector;
    private Iterable<String> contextParams;
    private final WebResourceManager webResourceManager;
    private final ProductAccessor productAccessor;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final BigPipe bigPipe;
    private final UserManager userManager;
    private final TemplateRenderer templateRenderer;

    private static final Logger log = LoggerFactory.getLogger(RemoteCondition.class);

    public RemoteCondition(WebResourceManager webResourceManager,
            ProductAccessor productAccessor, RemotablePluginAccessorFactory remotablePluginAccessorFactory,
            BigPipe bigPipe,
            UserManager userManager, TemplateRenderer templateRenderer)
    {
        this.webResourceManager = webResourceManager;
        this.productAccessor = productAccessor;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.bigPipe = bigPipe;
        this.userManager = userManager;
        this.templateRenderer = templateRenderer;
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
        webResourceManager.requireResource("com.atlassian.labs.remoteapps-plugin:remote-condition");

        Map<String,String> params = newHashMap();
        for (String contextParam : contextParams)
        {
            params.put(contextParam, templateRenderer.renderFragment(productAccessor.getLinkContextParams().get(contextParam), context));
        }
        String remoteUsername = userManager.getRemoteUsername();
        params.put("user_id", remoteUsername != null ? remoteUsername : "");
        Promise<String> responsePromise = remotablePluginAccessorFactory.get(pluginKey).executeAsyncGet(userManager.getRemoteUsername(),
                url, params, Collections.<String, String>emptyMap())
                .fold(
                        new Function<Throwable, String>()
                        {
                            @Override
                            public String apply(@Nullable Throwable input)
                            {
                                return "<script>AJS.log('Unable to retrieve remote condition from plugin '" + pluginKey + "');</script>";
                            }
                        },
                        new Function<String, String>()
                        {
                            @Override
                            public String apply(@Nullable String value)
                            {
                                try
                                {
                                    JSONObject obj = new JSONObject(value);
                                    if (obj.getBoolean("shouldDisplay"))
                                    {
                                        return "<script>AJS.$(\"" + toHideSelector + "\").removeClass('hidden').parent().removeClass('hidden');</script>";
                                    }
                                }
                                catch (JSONException e)
                                {
                                    log.warn("Invalid JSON returned from remote condition: " + value);
                                }
                                return "";
                            }
                        }
                );

        bigPipe.createContentHandler(RequestIdAccessor.getRequestId(), responsePromise);

        // always return true as the link will be disabled by default via the 'hidden' class
        return true;
    }
}
