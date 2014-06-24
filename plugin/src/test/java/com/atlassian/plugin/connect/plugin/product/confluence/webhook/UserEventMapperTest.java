package com.atlassian.plugin.connect.plugin.product.confluence.webhook;

import com.atlassian.confluence.event.events.user.UserCreateEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class UserEventMapperTest
{
    @Mock private UserManager userManager;
    @Mock private SettingsManager settingsManager;

    private UserEventMapper mapper;

    @Before
    public void setup()
    {
        this.mapper = new UserEventMapper(userManager, settingsManager);
    }

    @Test
    public void testUserProfileMapping()
    {
        User user = mock(User.class);
        UserProfile userProfile = mock(UserProfile.class);
        UserCreateEvent event = new UserCreateEvent(this, user);

        when(user.getName()).thenReturn("testuser");
        when(userManager.getUserProfile("testuser")).thenReturn(userProfile);

        when(userProfile.getEmail()).thenReturn("test@test.com");
        when(userProfile.getFullName()).thenReturn("Test User");
        when(userProfile.getUserKey()).thenReturn(new UserKey("123abc"));
        when(userProfile.getUsername()).thenReturn("testuser");

        Map<String, Object> map = mapper.toMap(event);

        assertThat(map, hasKey("userProfile"));
        Map<String, String> profileMap = (Map<String, String>)map.get("userProfile");

        assertThat(profileMap.get("username"), is("testuser"));
        assertThat(profileMap.get("email"), is("test@test.com"));
        assertThat(profileMap.get("fullName"), is("Test User"));
        assertThat(profileMap.get("userKey"), is("123abc"));
    }
}