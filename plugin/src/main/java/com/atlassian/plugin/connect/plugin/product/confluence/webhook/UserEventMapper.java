package com.atlassian.plugin.connect.plugin.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.user.UserEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.user.User;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class UserEventMapper extends ConfluenceEventMapper
{
    private final UserManager userManager;

    public UserEventMapper(final UserManager userManager, final SettingsManager confluenceSettingsManager)
    {
        super(userManager, confluenceSettingsManager);
        this.userManager = userManager;
    }

    @Override
    public boolean handles(final ConfluenceEvent e)
    {
        return e instanceof UserEvent;
    }

    @Override
    public Map<String, Object> toMap(final ConfluenceEvent event)
    {
        UserEvent userEvent = (UserEvent)event;
        User user = userEvent.getUser();

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(event));

        if (user != null)
        {
            // Fetch the corresponding UserProfile for the additional information it contains.
            UserProfile userProfile = userManager.getUserProfile(user.getName());
            if (userProfile != null)
            {
                builder.put("userProfile", userProfileToMap(userProfile));
            }
        }

        return builder.build();
    }
}
