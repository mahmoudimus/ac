package com.atlassian.plugin.connect.confluence.blueprint;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.atlassian.confluence.api.model.content.ContentBody;
import com.atlassian.confluence.api.model.content.ContentRepresentation;
import com.atlassian.confluence.api.service.content.ContentBodyConversionService;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintContextPostBody;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintContextValue;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.util.concurrent.Promise;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.api.request.HttpMethod.POST;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.IOUtils.toInputStream;

/**
 * A blueprint context provider that will make a remote request to the addon to generate a set of variables, used for later substitution.
 *
 */
public class BlueprintContextProvider extends AbstractBlueprintContextProvider
{
    private static final Logger log = LoggerFactory.getLogger(BlueprintContextProvider.class);

    private static final Gson gson = new Gson();
    private static final Type responseType = new ResponseTypeToken().getType();

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
    public BlueprintContextProvider(RemotablePluginAccessorFactory httpAccessor,
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
        log.debug("initializing " + BlueprintContextProvider.class.getSimpleName());

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
        log.debug("Blueprint '" + blueprintKey + "' in '" + addonKey + "' executing POST to '" + contextUrl + "'");

        Promise<String> promise = pluginAccessor.executeAsync(POST,
                                                              URI.create(contextUrl),
                                                              emptyMap(),
                                                              emptyMap(),
                                                              toInputStream(buildBody(blueprintContext)));
        String json = retrieveResponse(promise);
        List<BlueprintContextValue> contextMap = readJsonResponse(json);
        contextMap.forEach(v -> blueprintContext.put(v.getIdentifier(), transformValue(v)));
        return blueprintContext;
    }

    private List<BlueprintContextValue> readJsonResponse(String json)
    {
        List<BlueprintContextValue> contextMap = emptyList();
        try
        {
            log.debug("start parsing response json into " + responseType.getTypeName());

            contextMap = gson.fromJson(json, responseType);

            if (log.isDebugEnabled())
            {
                log.debug(Arrays.toString(contextMap.toArray()));
            }
            log.debug("finish parsing response json");
        }
        catch (JsonSyntaxException e)
        {
            log.info(String.format("Blueprint context json syntax error (%s,%s,%s). %s.", addonKey, blueprintKey, contextUrl, e.getMessage()));
            if (log.isDebugEnabled())
            {
                log.debug("response: " + json);
                log.debug("", e);
            }
            else
            {
                log.info("Turn on debug for " + log.getName() + " to see the full stackdebug.");
            }
            Throwables.propagate(e);
        }
        return contextMap;
    }

    private String retrieveResponse(Promise<String> promise)
    {
        String json = "{}";
        try
        {
            log.debug("start retrieving response");

            json = promise.get(MAX_TIMEOUT, SECONDS);

            log.debug("finished retrieving response");
            if (log.isDebugEnabled())
            {
                log.debug(json);
            }
        }
        catch (InterruptedException | ExecutionException | TimeoutException e)
        {
            log.info(String.format("Blueprint context retrieval error (%s,%s,%s). %s.", addonKey, blueprintKey, contextUrl, e.getMessage()));
            if (log.isDebugEnabled())
            {
                log.debug("", e);
            }
            else
            {
                log.info("Turn on debug for " + log.getName() + " to see the full stackdebug.");
            }
            Throwables.propagate(e);
        }
        return json;
    }

    private String transformValue(BlueprintContextValue v)
    {
        if (Iterables.contains(ContentRepresentation.INPUT_CONVERSION_TO_STORAGE_ORDER, ContentRepresentation.valueOf(v.getRepresentation())))
        {
            String converted = converter.convert(makeContentBody(v), ContentRepresentation.STORAGE).getValue();
            if (log.isDebugEnabled())
            {
                log.debug("converted " + v.getValue() + " to '" + converted + "'");
            }
            return converted;
        }
        else
        {
            return v.getValue();
        }
    }

    private ContentBody makeContentBody(BlueprintContextValue v)
    {
        return ContentBody.contentBodyBuilder()
                .representation(ContentRepresentation.valueOf(v.getRepresentation()))
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
        UserKey remoteUserKey = userManager.getRemoteUserKey();
        String userKey = remoteUserKey != null ? remoteUserKey.getStringValue() : "";
        Locale userLocale = Locale.ENGLISH;
        BlueprintContextPostBody body = new BlueprintContextPostBody(addonKey,
                                                                     blueprintKey,
                                                                     blueprintContext.getSpaceKey(),
                                                                     userKey, userLocale);
        //TODO: look into using a PipedInputStream/CircularBuffer if (when?) this body does becomes large (mitigates copying around large strings just to send it)
        String bodyJson = gson.toJson(body);
        if (log.isDebugEnabled())
        {
            log.debug("sending json " + bodyJson);
        }
        return bodyJson;
    }
}