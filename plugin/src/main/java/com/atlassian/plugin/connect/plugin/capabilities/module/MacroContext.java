package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.Spaced;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class MacroContext
{
    private final Map<String, Object> contextParameters;

    public MacroContext(ConversionContext conversionContext, UserProfile user)
    {
        Map<String, Object> ctx = newHashMap();

        ctx.put("output.type", conversionContext.getOutputType());

        ContentEntityObject entity = conversionContext.getEntity();

        String pageId = "";
        String pageTitle = "";
        String pageType = "";
        String spaceId = "";
        String spaceKey = "";
        String versionId = "";
        String userId = "";
        String userKey = "";

        if (entity != null)
        {
            pageId = entity.getIdAsString();
            pageTitle = StringUtils.defaultString(entity.getTitle());
            pageType = entity.getType();
            if (entity instanceof Spaced)
            {
                Space space = ((Spaced) entity).getSpace();
                spaceKey = space.getKey();
                spaceId = Long.toString(space.getId());
            }
            versionId = Integer.toString(entity.getVersion());
        }
        if (user != null)
        {
            userId = user.getUsername();
            userKey = user.getUserKey().getStringValue();
        }

        ctx.put("page.id", pageId);
        ctx.put("page.title", pageTitle);
        ctx.put("page.type", pageType);
        ctx.put("page.version.id", versionId);

        ctx.put("space.id", spaceId);
        ctx.put("space.key", spaceKey);

        ctx.put("user.id", userId);
        ctx.put("user.key", userKey);

        contextParameters = ImmutableMap.copyOf(ctx);
    }

    public Map<String, Object> getParameters()
    {
        return contextParameters;
    }
}
