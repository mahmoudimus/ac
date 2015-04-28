package com.atlassian.plugin.connect.confluence.webhook;

import java.util.Map;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.label.LabelEvent;
import com.atlassian.confluence.labels.Labelable;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;

import com.google.common.collect.ImmutableMap;

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
        Labelable labeled = event.getLabelled();
        if (labeled != null)
            builder.put("labeled", labelableToMap(labeled));

        return builder.build();
    }
}
