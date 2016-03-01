package com.atlassian.plugin.connect.confluence.webhook;

import com.atlassian.confluence.event.events.security.ContentPermissionEvent;
import com.atlassian.webhooks.spi.provider.EventMatcher;

/**
 * Currently, the ContentPermissionEvent is fired a little too aggressively. It gets twice during regular permission
 * updates. It also gets fired during a PageMove for good measure. In both of these cases, the redundant event is fired
 * with a null ContentPermission, so we filter those out.
 * See CONFDEV-25705.
 */
public class NonEmptyContentPermissionEventMatcher implements EventMatcher<ContentPermissionEvent> {
    @Override
    public boolean matches(ContentPermissionEvent contentPermissionEvent, Object o) {
        return contentPermissionEvent.getContentPermission() != null;
    }
}
