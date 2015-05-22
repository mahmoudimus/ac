package com.atlassian.plugin.connect.confluence.webhook;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.event.events.security.ContentPermissionEvent;
import com.atlassian.confluence.setup.settings.Settings;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentEventMapperTest
{
    @Mock private UserManager userManager;
    @Mock private SettingsManager settingsManager;

    private ContentEventMapper mapper;

    @Before
    public void setup()
    {
        this.mapper = new ContentEventMapper(userManager, settingsManager);
    }

    @Test
    public void testContentEventMapping()
    {
        ConfluenceUser user = mock(ConfluenceUser.class);
        when(user.getName()).thenReturn("testuser");
        when(user.getKey()).thenReturn(new UserKey("abc123"));

        ConfluenceUser user2 = mock(ConfluenceUser.class);
        when(user2.getName()).thenReturn("testuser2");
        when(user2.getKey()).thenReturn(new UserKey("321cba"));

        ContentEntityObject ceo = mock(ContentEntityObject.class);
        when(ceo.getCreator()).thenReturn(user);
        when(ceo.getLastModifier()).thenReturn(user2);
        when(ceo.getCreationDate()).thenReturn(new Date(123));
        when(ceo.getLastModificationDate()).thenReturn(new Date(321));
        when(ceo.getVersion()).thenReturn(123);
        when(ceo.getUrlPath()).thenReturn("/bar");

        Settings settings = mock(Settings.class);
        when(settings.getBaseUrl()).thenReturn("/foo");
        when(settingsManager.getGlobalSettings()).thenReturn(settings);

        Map<String, Object> map = mapper.toMap(new ContentPermissionEvent(this, ceo, null));

        assertTrue(map.containsKey("content"));
        Map<String, Object> contentMap = (Map<String, Object>)map.get("content");

        assertEquals("testuser", contentMap.get("creatorName"));
        assertEquals("abc123", contentMap.get("creatorKey"));
        assertEquals("testuser2", contentMap.get("lastModifierName"));
        assertEquals("321cba", contentMap.get("lastModifierKey"));
        assertEquals(123L, contentMap.get("creationDate"));
        assertEquals(321L, contentMap.get("modificationDate"));
        assertEquals(123, contentMap.get("version"));
        assertEquals("/foo/bar", contentMap.get("self"));
    }
}
