package com.atlassian.plugin.connect.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.plugins.mentions.api.ConfluenceMentionEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.user.User;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ConfluenceMentionEventMapper extends ConfluenceEventMapper {

    private final UserManager userManager;

    public ConfluenceMentionEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager) {
        super(userManager, confluenceSettingsManager);
        this.userManager = userManager;
    }

    @Override
    public boolean handles(ConfluenceEvent e) {
        return e instanceof ConfluenceMentionEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent event) {

        ConfluenceMentionEvent mention = (ConfluenceMentionEvent) event;
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("mentionedUser", userProfileToMap(mention.getMentionedUserProfile()));

        User mentioningUser = mention.getMentioningUser();

        if (mentioningUser != null) {
            UserProfile mentioningUserProfile = userManager.getUserProfile(mentioningUser.getName());
            if (mentioningUserProfile != null) {
                builder.put("mentioningUser", userProfileToMap(mentioningUserProfile));
            }
        }

        builder.put("mentionHtml", mention.getMentionHtml());
        builder.put("content", contentEntityObjectToMap(mention.getContent()));

        return builder.build();
    }
}