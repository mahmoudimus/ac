package com.atlassian.plugin.connect.confluence.blueprint;

import java.lang.reflect.Type;
import java.util.Map;

import com.atlassian.confluence.api.model.content.ContentBody;
import com.atlassian.confluence.api.model.content.ContentRepresentation;
import com.atlassian.confluence.api.service.content.ContentBodyConversionService;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.util.concurrent.Promise;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.confluence.api.model.content.ContentBody.contentBodyBuilder;
import static com.atlassian.confluence.api.model.content.ContentRepresentation.INPUT_CONVERSION_TO_STORAGE_ORDER;
import static com.atlassian.confluence.api.model.content.ContentRepresentation.PLAIN;
import static com.atlassian.confluence.api.model.content.ContentRepresentation.STORAGE;
import static com.atlassian.confluence.api.model.content.ContentRepresentation.valueOf;
import static com.atlassian.plugin.connect.api.request.HttpMethod.POST;
import static com.google.common.collect.Iterables.contains;
import static java.net.URI.create;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A blueprint context provider that will make a remote request to the addon to generate a set of variables, used for later substitution.
 *
 * @since 1.1.60
 */
public class ConnectBlueprintContextProvider extends AbstractBlueprintContextProvider
{
    private static final Logger log = LoggerFactory.getLogger(ConnectBlueprintContextProvider.class);

    /* this is a type token so that Gson can deserialize a map back from a json object*/
    private static final Type returnType = new TypeToken<Map<String, BlueprintContextValue>>(){}.getType();
    private static final Gson GSON = new Gson();

    /* timeout in seconds for remote requests to get context values*/
    private static final long MAX_TIMEOUT = 3L;

    static final String CONTEXT_URL_KEY = "context-url";
    static final String ADDON_KEY = "addon-key";
    static final String CONTENT_TEMPLATE_KEY = "content-template-key";

    private final RemotablePluginAccessorFactory factory;
    private final ContentBodyConversionService converter;

    /*TODO: check if these fields need to be either volatile due to it being set by a different thread than the thread reading it.*/
    private String contextUrl;
    private String addonKey;
    private String blueprintKey;

    @Autowired
    public ConnectBlueprintContextProvider(RemotablePluginAccessorFactory httpAccessor,
                                           ContentBodyConversionService converter)
    {
        factory = httpAccessor;
        this.converter = converter;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        super.init(params);
        Preconditions.checkArgument(params.containsKey(CONTEXT_URL_KEY), "the " + CONTEXT_URL_KEY + " is not specified. The context-provider element should not have been supplied if the connect addon blueprint module did not provide a context provider url");
        Preconditions.checkArgument(params.containsKey(ADDON_KEY), "the " + ADDON_KEY + " is not specified.");
        Preconditions.checkArgument(params.containsKey(CONTENT_TEMPLATE_KEY), "the " + CONTENT_TEMPLATE_KEY + " is not specified");
        contextUrl = params.get(CONTEXT_URL_KEY);
        addonKey = params.get(ADDON_KEY);
        blueprintKey = params.get(CONTENT_TEMPLATE_KEY);
    }

    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext blueprintContext)
    {
        try
        {
            log.trace("executing POST to " + contextUrl);

            RemotablePluginAccessor accessor = factory.get(addonKey);
            Promise<String> request = accessor.executeAsync(POST, create(contextUrl), buildBody(blueprintContext), emptyMap());
            String json = request.get(MAX_TIMEOUT, SECONDS);
            Map<String, BlueprintContextValue> contextMap = GSON.fromJson(json, returnType);
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
        if (contains(INPUT_CONVERSION_TO_STORAGE_ORDER, valueOf(v.representation)))
        {
            return converter.convert(makeContentBody(v), STORAGE)
                            .getValue();
        }
        else
        {
            return v.value;
        }
    }

    private ContentBody makeContentBody(BlueprintContextValue v)
    {
        return contentBodyBuilder()
                .representation(valueOf(v.representation))
                .value(v.value)
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

    private Map<String, String[]> buildBody(BlueprintContext blueprintContext)
    {
        PostBody body = new PostBody();
        body.spaceKey = blueprintContext.getSpaceKey();
        body.addonKey = addonKey;
        body.userKey = AuthenticatedUserThreadLocal.get().getKey().getStringValue();
        body.blueprintKey = blueprintKey;

        return asMap(body);
    }

    private Map<String, String[]> asMap(PostBody body)
    {
        return ImmutableMap.of(
                "addonKey", new String[] { body.addonKey },
                "blueprintKey", new String[] { body.blueprintKey },
                "userKey", new String[] { body.userKey },
                "spaceKey", new String[] { body.spaceKey }
        );
    }

    /**
     * Pojo to hold the body POST'ed to the addon's blueprint context url.
     */
    private static final class PostBody
    {
        private String addonKey;
        private String blueprintKey;
        private String spaceKey;
        private String userKey;
        private String parentPageId;
    }

    /**
     * Pojo representing the format of the context returned from the {@link #contextUrl}.
     * {@link #representation} is one of {@link ContentRepresentation#PLAIN} or {@link ContentRepresentation#STORAGE} only.
     * Other representations are invalid.
     */
    private static final class BlueprintContextValue
    {
        private String value = "";
        private String representation = PLAIN.getRepresentation();
    }
}
