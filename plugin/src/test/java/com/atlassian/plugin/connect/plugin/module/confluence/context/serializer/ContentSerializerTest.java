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
    public void testSerializeContent()
    {
        Map<String, Object> contentSerialized = serializeAndGetResult(createEntity(ContentEntityObject.class));
        assertNotNull(contentSerialized);
    }

    @Test
    public void testSerializeContentHasId()
    {
        Map<String, Object> contentSerialized = serializeAndGetResult(createEntity(ContentEntityObject.class));
        assertEquals(123L, contentSerialized.get("id"));
    }

    @Test
    public void testSerializeContentHasVersion()
    {
        Map<String, Object> contentSerialized = serializeAndGetResult(createEntity(ContentEntityObject.class));
        assertEquals(3, contentSerialized.get("version"));
    }

    @Test
    public void testSerializeContentHasType()
    {
        Map<String, Object> contentSerialized = serializeAndGetResult(createEntity(ContentEntityObject.class));
        assertEquals("page", contentSerialized.get("type"));
    }

    @Test
    public void testSerializeContentDoesNotHavePlugin()
    {
        Map<String, Object> contentSerialized = serializeAndGetResult(createEntity(ContentEntityObject.class));
        assertNull(contentSerialized.get("plugin"));
    }

    @Test
    public void testSerializeCustomContentHasPlugin()
    {
        CustomContentEntityObject ceo = createEntity(CustomContentEntityObject.class);
        when(ceo.getPluginModuleKey()).thenReturn("plugin:foo");
        Map<String, Object> contentSerialized = serializeAndGetResult(ceo);

        assertEquals("plugin:foo", contentSerialized.get("plugin"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> serializeAndGetResult(ContentEntityObject ceo)
    {
        Map<String, Object> serialized = contentSerializer.serialize(ceo);
        return (Map<String, Object>) serialized.get("content");
    }

    private <T extends ContentEntityObject> T createEntity(Class<T> clazz)
    {
        T content = mock(clazz);
        when(content.getId()).thenReturn(123L);
        when(content.getVersion()).thenReturn(3);
        when(content.getType()).thenReturn("page");
        return content;
    }
}
