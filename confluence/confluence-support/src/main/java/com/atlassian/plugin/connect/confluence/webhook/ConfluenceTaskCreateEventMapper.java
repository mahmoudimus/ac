package com.atlassian.plugin.connect.confluence.webhook;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.plugins.tasklist.event.ConfluenceTaskV2CreateEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ConfluenceTaskCreateEventMapper extends ConfluenceEventMapper {

    private final UserManager userManager;

    public ConfluenceTaskCreateEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager) {
        super(userManager, confluenceSettingsManager);
        this.userManager = userManager;
    }

    @Override
    public boolean handles(ConfluenceEvent event) {
        return event instanceof ConfluenceTaskV2CreateEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent event) {
        ConfluenceTaskV2CreateEvent taskEvent = (ConfluenceTaskV2CreateEvent) event;
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(event));
        builder.put("task", taskToMap(taskEvent));
        return builder.build();
    }

    private Map<String, Object> taskToMap(ConfluenceTaskV2CreateEvent taskEvent) {

        ImmutableMap.Builder<String, Object> task = ImmutableMap.builder();
        UserProfile assignee = userManager.getUserProfile(taskEvent.getAssigneeUserKey());
        if (assignee != null) {
            task.put("assignee", userProfileToMap(assignee));
        }
        UserProfile creator = userManager.getUserProfile(taskEvent.getTask().getCreator());
        if (creator != null) {
            task.put("creator", userProfileToMap(creator));
        }
        task.put("body", taskEvent.getTask().getBody());
        task.put("status", taskEvent.getTask().getStatusAsString());
        task.put("creationDate", taskEvent.getTask().getCreateDate().getTime());

        if (taskEvent.getSource() instanceof ContentEntityObject) {
            ContentEntityObject content = (ContentEntityObject) taskEvent.getSource();
            task.put("content", contentEntityObjectToMap(content));
        }

        return task.build();
    }
}