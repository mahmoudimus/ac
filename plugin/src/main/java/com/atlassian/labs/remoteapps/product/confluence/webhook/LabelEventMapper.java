package com.atlassian.labs.remoteapps.product.confluence.webhook;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.label.LabelEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class LabelEventMapper extends ConfluenceEventMapper
{
    public LabelEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        super(userManager, confluenceSettingsManager);
    }

    public boolean handles(ConfluenceEvent e)
    {
        return e instanceof LabelEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent e)
    {
        LabelEvent event = (LabelEvent) e;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(event));
        builder.put("label", labelToMap(event.getLabel()));
        if (event.getLabelled() instanceof ContentEntityObject)
        {
            builder.put("labeled", contentEntityObjectToMap((ContentEntityObject) event.getLabelled()));
        }
        else
        {
            builder.put("labeled", event.getLabelled().getClass().getName()); // TODO: Improve this.
        }
        return builder.build();
    }
}
