package com.atlassian.plugin.connect.plugin.util.http;

import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.Map;

/**
 * Allows to take a snaphot of the session state and restore it later on
 */
public class SessionSnapshot
{
    private final Map<String, Object> snapshot;

    private SessionSnapshot(@Nonnull Map<String, Object> snapshot)
    {
        this.snapshot = snapshot;
    }

    public static SessionSnapshot take(@Nonnull HttpSession session)
    {
        Map<String, Object> snapShot = Maps.newHashMap();
        Enumeration attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements())
        {
            String name = attributeNames.nextElement().toString();
            Object value = session.getAttribute(name);
            snapShot.put(name, value);
        }
        return new SessionSnapshot(snapShot);
    }

    public void restore(@Nonnull HttpSession session)
    {
        Enumeration attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements())
        {
            String name = attributeNames.nextElement().toString();
            session.removeAttribute(name);
        }
        for (Map.Entry<String, Object> entry : snapshot.entrySet())
        {
            session.setAttribute(entry.getKey(), entry.getValue());
        }
    }
}
