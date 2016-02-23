package com.atlassian.plugin.connect.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.plugins.createcontent.api.events.BlueprintPageCreateEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class BlueprintPageCreateEventMapper extends ConfluenceEventMapper {
    public BlueprintPageCreateEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager) {
        super(userManager, confluenceSettingsManager);
    }

    @Override
    public boolean handles(ConfluenceEvent e) {
        return e instanceof BlueprintPageCreateEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent e) {
        BlueprintPageCreateEvent event = (BlueprintPageCreateEvent) e;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(e));

        builder.put("blueprintContext", event.getContext());
        builder.put("blueprint", contentBlueprintToMap(event.getBlueprint()));
        builder.put("creator", event.getCreator().getKey().getStringValue());
        builder.put("page", contentEntityObjectToMap(event.getPage()));

        return builder.build();
    }
}