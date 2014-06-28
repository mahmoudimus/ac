package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

@ConfluenceComponent
public class ContentSerializer implements ParameterSerializer<ContentEntityObject>
{
    @Override
    public Map<String, Object> serialize(ContentEntityObject content)
    {
        ImmutableMap.Builder<String, Object> contentMap = ImmutableMap.builder();
        contentMap.put("id", content.getId());
        contentMap.put("version", content.getVersion());
        contentMap.put("type", content.getType());

        if (content instanceof CustomContentEntityObject)
        {
            CustomContentEntityObject customContent = (CustomContentEntityObject)content;
            contentMap.put("plugin", customContent.getPluginModuleKey());
        }

        return ImmutableMap.<String, Object>of("content", contentMap.build());
    }
}
