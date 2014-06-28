package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.core.ContentEntityObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentSerializerTest
{
    private ContentSerializer contentSerializer;

    @Before
    public void setup()
    {
        this.contentSerializer = new ContentSerializer();
    }

    @Test
    public void testSerializeRegularContent()
    {
        ContentEntityObject content = mock(ContentEntityObject.class);
        when(content.getId()).thenReturn(123L);
        when(content.getVersion()).thenReturn(3);
        when(content.getType()).thenReturn("page");

        Map<String, Object> serialized = contentSerializer.serialize(content);
        Map<String, Object> contentSerialized = (Map<String, Object>)serialized.get("content");
        assertNotNull(contentSerialized);

        assertEquals(123L, contentSerialized.get("id"));
        assertEquals(3, contentSerialized.get("version"));
        assertEquals("page", contentSerialized.get("type"));
        assertNull(contentSerialized.get("plugin"));
    }

    @Test
    public void testSerializeCustomContent()
    {
        CustomContentEntityObject content = mock(CustomContentEntityObject.class);
        when(content.getId()).thenReturn(123L);
        when(content.getVersion()).thenReturn(3);
        when(content.getType()).thenReturn("page");
        when(content.getPluginModuleKey()).thenReturn("plugin:foo");

        Map<String, Object> serialized = contentSerializer.serialize(content);
        Map<String, Object> contentSerialized = (Map<String, Object>)serialized.get("content");
        assertNotNull(contentSerialized);

        assertEquals(123L, contentSerialized.get("id"));
        assertEquals(3, contentSerialized.get("version"));
        assertEquals("page", contentSerialized.get("type"));
        assertEquals("plugin:foo", contentSerialized.get("plugin"));
    }
}
