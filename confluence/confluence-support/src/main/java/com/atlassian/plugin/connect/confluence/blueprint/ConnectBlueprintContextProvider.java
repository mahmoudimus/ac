package com.atlassian.plugin.connect.confluence.blueprint;

import java.lang.reflect.Type;
import java.util.Map;

import com.atlassian.confluence.api.model.content.ContentBody;
import com.atlassian.confluence.api.model.content.ContentRepresentation;
import com.atlassian.confluence.api.service.content.ContentBodyConversionService;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.util.concurrent.Promise;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.api.request.HttpMethod.POST;
import static java.net.URI.create;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.IOUtils.toInputStream;

/**
 * A blueprint context provider that will make a remote request to the addon to generate a set of variables, used for later substitution.
 *
 * @since 1.1.60
 */
public class ConnectBlueprintContextProvider extends AbstractBlueprintContextProvider
{
    private static final Logger log = LoggerFactory.getLogger(ConnectBlueprintContextProvider.class);

    private static final Gson GSON = new Gson();
    private static final Type responseType = new ConnectBlueprintContextResponseTypeToken().getType();

    /* timeout in seconds for remote requests to get context values. Should be same value as set in ConnectHttpClientFactory*/
    private static final long MAX_TIMEOUT = 10L;
    /*the name of the param holding the context url*/
    static final String CONTEXT_URL_KEY = "context-url";
    /*the name of the param holding the key of the addon that created this module*/
    static final String REMOTE_ADDON_KEY = "addon-key";
    /*the name of the param holding the key of the content template for this context provider*/
    static final String CONTENT_TEMPLATE_KEY = "content-template-key";

    private final RemotablePluginAccessorFactory accessorFactory;
    private final ContentBodyConversionService converter;
    private final UserManager userManager;

    private String contextUrl;
    private String addonKey;
    private String blueprintKey;
    private RemotablePluginAccessor pluginAccessor;

    @Autowired
    public ConnectBlueprintContextProvider(RemotablePluginAccessorFactory httpAccessor,
                                           ContentBodyConversionService converter,
                                           UserManager userManager)
    {
        accessorFactory = httpAccessor;
        this.converter = converter;
        this.userManager = userManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        super.init(params);
        checkContainsKey(params, CONTEXT_URL_KEY, "the " + CONTEXT_URL_KEY + " is not specified. "
                                              + "The context-provider element should not have been supplied "
                                              + "if the connect addon blueprint module did not provide a context provider url");
        checkContainsKey(params, REMOTE_ADDON_KEY, "the " + REMOTE_ADDON_KEY + " is not specified.");
        checkContainsKey(params, CONTENT_TEMPLATE_KEY, "the " + CONTENT_TEMPLATE_KEY + " is not specified");

        contextUrl = params.get(CONTEXT_URL_KEY);
        addonKey = params.get(REMOTE_ADDON_KEY);
        blueprintKey = params.get(CONTENT_TEMPLATE_KEY);
        pluginAccessor = accessorFactory.getOrThrow(addonKey);
    }

    private static void checkContainsKey(Map<String, String> params, String key, String message)
    {
        if (!params.containsKey(key))
        {
            throw new PluginParseException(message);
        }
    }

    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext blueprintContext)
    {

        log.trace("executing POST to " + contextUrl);
        try
        {
            Promise<String> promise = pluginAccessor.executeAsync(POST,
                                                                  create(contextUrl),
                                                                  emptyMap(),
                                                                  emptyMap(),
                                                                  toInputStream(buildBody(blueprintContext)));
            String json = promise.get(MAX_TIMEOUT, SECONDS);
            Map<String, BlueprintContextValue> contextMap = GSON.fromJson(json, responseType);
            contextMap.forEach((k,v) -> blueprintContext.put(k, transformValue(v)));
        }
        catch (Exception e)
        {
            log.info(String.format("Blueprint error (%s,%s,%s). %s", addonKey, blueprintKey, contextUrl, e.getMessage()));
            if (log.isDebugEnabled())
            {
                log.debug("", e);
            }
            Throwables.propagate(e);
        }
        return blueprintContext;
    }

    private String transformValue(BlueprintContextValue v)
    {
        if (Iterables.contains(ContentRepresentation.INPUT_CONVERSION_TO_STORAGE_ORDER, v.getRepresentation()))
        {
            return converter.convert(makeContentBody(v), ContentRepresentation.STORAGE).getValue();
        }
        else
        {
            return v.getValue();
        }
    }

    private ContentBody makeContentBody(BlueprintContextValue v)
    {
        return ContentBody.contentBodyBuilder()
                .representation(v.getRepresentation())
                .value(v.getValue())
                .build();
    }

    @VisibleForTesting
    public String getAddonKey()
    {
        return addonKey;
    }

    @VisibleForTesting
    public String getBlueprintKey()
    {
        return blueprintKey;
    }

    @VisibleForTesting
    public String getContextUrl()
    {
        return contextUrl;
    }

    private String buildBody(BlueprintContext blueprintContext)
    {
        ConnectBlueprintContextPostBody body = new ConnectBlueprintContextPostBody();
        UserKey remoteUserKey = userManager.getRemoteUserKey();
        body.spaceKey = blueprintContext.getSpaceKey();
        body.addonKey = addonKey;
        body.userKey = remoteUserKey != null ? remoteUserKey.getStringValue() : "";
        body.blueprintKey = blueprintKey;

        //TODO: look into using a PipedInputStream/CircularBuffer if (when?) this body does becomes large (mitigates copying around large strings just to send it)
        return GSON.toJson(body);
    }
}