package com.atlassian.plugin.connect.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.plugins.mentions.api.ConfluenceMentionEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ConfluenceMentionEventMapper extends ConfluenceEventMapper {

    private final UserManager userManager;

    public ConfluenceMentionEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager) {
        super(userManager, confluenceSettingsManager);
        this.userManager = userManager;
    }

    @Override
    public boolean handles(ConfluenceEvent event) {
        return event instanceof ConfluenceMentionEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent event) {
        ConfluenceMentionEvent mentionEvent = (ConfluenceMentionEvent) event;
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(event));
        builder.put("mention", mentionToMap(mentionEvent));
        return builder.build();
    }

    private Map<String, Object> mentionToMap(ConfluenceMentionEvent mentionEvent) {
        ImmutableMap.Builder<String, Object> mention = ImmutableMap.builder();
        mention.put("mentionedUser", userProfileToMap(mentionEvent.getMentionedUserProfile()));
        ConfluenceUser source = mentionEvent.getMentioningUser();
        if (source != null) {
            UserProfile profile = userManager.getUserProfile(source.getKey());
            if (profile != null) {
                mention.put("mentioningUser", userProfileToMap(profile));
            }
        }
        mention.put("mentionHtml", mentionEvent.getMentionHtml());
        mention.put("content", contentEntityObjectToMap(mentionEvent.getContent()));
        return mention.build();
    }
}