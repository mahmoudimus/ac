package com.atlassian.plugin.connect.plugin.iframe.context.confluence;

import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.core.ContentEntityObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfluenceModuleContextParametersImplTest
{
    private ConfluenceModuleContextParametersImpl moduleContextParameters;

    @Before
    public void setup()
    {
        this.moduleContextParameters = new ConfluenceModuleContextParametersImpl();
    }

    @Test
    public void testPutContentHasCorrectId()
    {
        moduleContextParameters.addContent(createEntity(ContentEntityObject.class));

        assertEquals("123", moduleContextParameters.get("content.id"));
    }

    @Test
    public void testPutContentHasCorrectVersion()
    {
        moduleContextParameters.addContent(createEntity(ContentEntityObject.class));
        assertEquals("3", moduleContextParameters.get("content.version"));
    }

    @Test
    public void testPutContentHasCorrectType()
    {
        moduleContextParameters.addContent(createEntity(ContentEntityObject.class));
        assertEquals("page", moduleContextParameters.get("content.type"));
    }

    @Test
    public void testPutContentHasNoPluginKey()
    {
        moduleContextParameters.addContent(createEntity(ContentEntityObject.class));
        assertNull(moduleContextParameters.get("content.plugin"));
    }

    @Test
    public void testPutCustomContentHasPluginKey()
    {
        CustomContentEntityObject content = mock(CustomContentEntityObject.class);
        when(content.getPluginModuleKey()).thenReturn("plugin:foo");

        moduleContextParameters.addContent(content);
        assertEquals("plugin:foo", moduleContextParameters.get("content.plugin"));
    }

    private <T extends ContentEntityObject> T createEntity (Class<T> clazz)
    {
        T content = mock(clazz);
        when(content.getId()).thenReturn(123L);
        when(content.getVersion()).thenReturn(3);
        when(content.getType()).thenReturn("page");
        return content;
    }
}
