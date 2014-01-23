package com.atlassian.plugin.connect.test.plugin.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.security.LoginEvent;
import com.atlassian.confluence.event.events.security.LoginFailedEvent;
import com.atlassian.confluence.event.events.security.LogoutEvent;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.connect.plugin.product.confluence.webhook.SecurityEventMapper;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith (MockitoJUnitRunner.class)
public class SecurityEventMapperTest
{
    @Mock private UserManager userManager;
    @Mock private SettingsManager confluenceSettingsManager;

    private static String EVENT_USERNAME = "bruceWayne";
    private static String EVENT_SESSION_ID = "session-id";
    private static String EVENT_HOST_NAME = "batman.com";
    private static String EVENT_HOST_IP = "192.312.405.1";


    @Test
    public void testHandlesLoginEvent() throws Exception
    {
        SecurityEventMapper eventMapper = new SecurityEventMapper(userManager, confluenceSettingsManager);
        LoginEvent event = new LoginEvent(this, EVENT_USERNAME, EVENT_SESSION_ID, EVENT_HOST_NAME, EVENT_HOST_IP, LoginEvent.DIRECT);
        assertTrue("Security event mapper should handle a LoginEvent", eventMapper.handles(event));
    }

    @Test
    public void testHandlesLoginFailedEvent() throws Exception
    {
        SecurityEventMapper eventMapper = new SecurityEventMapper(userManager, confluenceSettingsManager);
        LoginFailedEvent event = new LoginFailedEvent(this, EVENT_USERNAME, EVENT_SESSION_ID, EVENT_HOST_NAME, EVENT_HOST_IP);
        assertTrue("Security event mapper should handle a LoginFailedEvent", eventMapper.handles(event));
    }

    @Test
    public void testHandlesLogoutEvent() throws Exception
    {
        SecurityEventMapper eventMapper = new SecurityEventMapper(userManager, confluenceSettingsManager);
        LogoutEvent event = new LogoutEvent(this, EVENT_USERNAME, EVENT_SESSION_ID, EVENT_HOST_NAME, EVENT_HOST_IP);
        assertTrue("Security event mapper should handle a LogoutEvent", eventMapper.handles(event));
    }

    @Test
    public void testDoesntHandleConfluenceEvent() throws Exception
    {
        SecurityEventMapper eventMapper = new SecurityEventMapper(userManager, confluenceSettingsManager);
        ConfluenceEvent event = new ConfluenceEvent(this) { };
        assertFalse("Security event mapper should not handle a ConfluenceEvent", eventMapper.handles(event));
    }

    @Test
    public void testLoginEventSerialisation() throws Exception
    {
        SecurityEventMapper eventMapper = new SecurityEventMapper(userManager, confluenceSettingsManager);
        LoginEvent event = new LoginEvent(this, EVENT_USERNAME, EVENT_SESSION_ID, EVENT_HOST_NAME, EVENT_HOST_IP, LoginEvent.DIRECT);
        Map<String, Object> map = eventMapper.toMap(event);

        assertNotNull(map);
        assertNotNull("Event map should contain timestamp", map.get("timestamp"));
        assertEquals("Event map should contain username", EVENT_USERNAME, map.get("user"));
        assertEquals("Event map should contain hostname", EVENT_HOST_NAME, map.get("remoteHost"));
        assertEquals("Event map should contain host ip", EVENT_HOST_IP, map.get("remoteIP"));
        assertFalse("Event map should NOT contain sessionId", map.containsValue(EVENT_SESSION_ID));
    }

    @Test
    public void testLoginFailedEventSerialisation() throws Exception
    {
        SecurityEventMapper eventMapper = new SecurityEventMapper(userManager, confluenceSettingsManager);
        LoginFailedEvent event = new LoginFailedEvent(this, EVENT_USERNAME, EVENT_SESSION_ID, EVENT_HOST_NAME, EVENT_HOST_IP);
        Map<String, Object> map = eventMapper.toMap(event);

        assertNotNull(map);
        assertNotNull("Event map should contain timestamp", map.get("timestamp"));
        assertEquals("Event map should contain username", EVENT_USERNAME, map.get("user"));
        assertEquals("Event map should contain hostname", EVENT_HOST_NAME, map.get("remoteHost"));
        assertEquals("Event map should contain host ip", EVENT_HOST_IP, map.get("remoteIP"));
        assertFalse("Event map should NOT contain sessionId", map.containsValue(EVENT_SESSION_ID));
    }

    @Test
    public void testLogoutEventSerialisation() throws Exception
    {
        SecurityEventMapper eventMapper = new SecurityEventMapper(userManager, confluenceSettingsManager);
        LogoutEvent event = new LogoutEvent(this, EVENT_USERNAME, EVENT_SESSION_ID, EVENT_HOST_NAME, EVENT_HOST_IP);
        Map<String, Object> map = eventMapper.toMap(event);

        assertNotNull(map);
        assertNotNull("Event map should contain timestamp", map.get("timestamp"));
        assertEquals("Event map should contain username", EVENT_USERNAME, map.get("user"));
        assertEquals("Event map should contain hostname", EVENT_HOST_NAME, map.get("remoteHost"));
        assertEquals("Event map should contain host ip", EVENT_HOST_IP, map.get("remoteIP"));
        assertFalse("Event map should NOT contain sessionId", map.containsValue(EVENT_SESSION_ID));
    }
}
