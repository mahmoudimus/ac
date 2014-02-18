package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.web.Condition;
import com.atlassian.util.concurrent.Promise;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A condition that evaluates based on the response from a remote end-point hosted by the add-on.
 */
public class AddOnCondition implements Condition
{
    public static final String URL = "url";
    public static final String ADDON_KEY = "addOnKey";

    private static final Logger log = LoggerFactory.getLogger(AddOnCondition.class);

    private interface Configuration
    {
        String getUrl();
        String getAddOnKey();
    }

    private final AtomicReference<Configuration> configuration = new AtomicReference<Configuration>();

    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final WebFragmentModuleContextExtractor webFragmentModuleContextExtractor;

    public AddOnCondition(final RemotablePluginAccessorFactory remotablePluginAccessorFactory,
            final IFrameUriBuilderFactory iFrameUriBuilderFactory,
            final WebFragmentModuleContextExtractor webFragmentModuleContextExtractor)
    {
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
    }

    /**
     * Initializer used when created as a plugins2 web fragment condition.
     */
    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
        Configuration cfg = new ConfigurationImpl(
            checkNotNull(params.get(ADDON_KEY), ADDON_KEY),
            checkNotNull(params.get(URL), URL)
        );
        configuration.set(cfg);
    }

    @Override
    public boolean shouldDisplay(final Map<String, Object> context)
    {
        Configuration cfg = checkNotNull(configuration.get(), "configuration has not been set - init() not called?");

        ModuleContextParameters moduleContext = webFragmentModuleContextExtractor.extractParameters(context);

        String uri = iFrameUriBuilderFactory
                .builder()
                .addOn(cfg.getAddOnKey())
                .namespace("condition") // namespace is not really important as we're not rendering an iframe
                .urlTemplate(cfg.getUrl())
                .context(moduleContext)
                .sign(false)
                .build();

        Promise<String> responsePromise = remotablePluginAccessorFactory.getOrThrow(cfg.getAddOnKey())
                .executeAsync(HttpMethod.GET, URI.create(uri),
                        Collections.<String, String[]>emptyMap(), Collections.<String, String>emptyMap());

        String response;
        try
        {
            response = responsePromise.claim();
        }
        catch (Exception e)
        {
            log.warn(String.format("Request to addon condition URL failed: %s", cfg), e);
            return false;
        }

        try
        {
            JSONObject obj = (JSONObject) JSONValue.parseWithException(response);
            Object shouldDisplay = obj.get("shouldDisplay");
            if (shouldDisplay instanceof Boolean)
            {
                return (Boolean) shouldDisplay;
            }
            else if (shouldDisplay instanceof String)
            {
                return Boolean.parseBoolean((String) shouldDisplay);
            }
            else
            {
                log.warn(String.format("Malformed response from addon condition URL: %s\nExpected a boolean value "
                        + "but was " + shouldDisplay, cfg));
                return false;
            }
        }
        catch (Exception e)
        {
            log.warn(String.format("Malformed response from addon condition URL: %s", cfg), e);
            return false;
        }
    }

    private static final class ConfigurationImpl implements Configuration
    {
        private final String addonKey;
        private final String url;

        private ConfigurationImpl(String addonKey, String url)
        {
            this.addonKey = addonKey;
            this.url = url;
        }

        public String getAddOnKey()
        {
            return addonKey;
        }

        public String getUrl()
        {
            return url;
        }

        @Override
        public String toString()
        {
            return String.format("Condition for %s at %s", addonKey, url);
        }
    }
}
