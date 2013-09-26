package com.atlassian.plugin.connect.plugin.module.context;

import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Serializes a web-panel's context to URL parameters which will be included in web-panel's iframe URL.
 */
@Component
public class ContextMapURLSerializer
{
    private final List<ContextMapParameterExtractor> contextMapParameterExtractors;

    public ContextMapURLSerializer()
    {
        this(Collections.<ContextMapParameterExtractor>emptyList());
    }

    @Autowired(required = false)
    public ContextMapURLSerializer(
            List<ContextMapParameterExtractor> contextMapParameterExtractors)
    {
        this.contextMapParameterExtractors = checkNotNull(contextMapParameterExtractors);
    }

    public Map<String, Object> getExtractedWebPanelParameters(final Map<String, Object> context)
    {
        return getExtractedWebPanelParameters(context, Objects.toString(context.get(JiraWebInterfaceManager.CONTEXT_KEY_USER)));
    }

    public Map<String, Object> getExtractedWebPanelParameters(final Map<String, Object> context, String username)
    {
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object>builder();
        for (ContextMapParameterExtractor extractor : contextMapParameterExtractors)
        {
            Optional<Object> resource = extractor.extract(context);
            // TODO: The extractor.hasViewPermission is unnecessary here. Remove
            if (resource.isPresent() /*&& extractor.hasViewPermission(username, resource.get())*/)
            {
                builder.putAll(extractor.serializer().serialize(resource.get()));
            }
        }
        return builder.build();
    }

    public  Map<String, Object> getAuthenticatedAddonParameters(final Map<String, Object> context, String username)
            throws UnauthorisedException, ResourceNotFoundException, MalformedRequestException
    {
        /*
         * We just pass through all the deserialisers. If no exception then we return the original context.
          * TODO: This seems weird now that we aren't checking permissions explicitly here
         */
        for (ContextMapParameterExtractor extractor : contextMapParameterExtractors)
        {
            final ParameterDeserializer deserializer = extractor.deserializer();
            // As both Confluence and Jira deserialisers now check permissions and throw appropriate exceptions
            // its enough to run the deserialiser without checking result
            deserializer.deserialize(context, username);
        }
        return context;

    }
}
