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
    public void testPutContent()
    {
        ContentEntityObject content = mock(ContentEntityObject.class);
        when(content.getId()).thenReturn(123L);
        when(content.getVersion()).thenReturn(3);
        when(content.getType()).thenReturn("page");

        moduleContextParameters.addContent(content);
        assertEquals("123", moduleContextParameters.get("content.id"));
        assertEquals("3", moduleContextParameters.get("content.version"));
        assertEquals("page", moduleContextParameters.get("content.type"));
        assertNull(moduleContextParameters.get("content.plugin"));
    }

    @Test
    public void testPutCustomContent()
    {
        CustomContentEntityObject content = mock(CustomContentEntityObject.class);
        when(content.getId()).thenReturn(123L);
        when(content.getVersion()).thenReturn(3);
        when(content.getType()).thenReturn("page");
        when(content.getPluginModuleKey()).thenReturn("plugin:foo");

        moduleContextParameters.addContent(content);
        assertEquals("123", moduleContextParameters.get("content.id"));
        assertEquals("3", moduleContextParameters.get("content.version"));
        assertEquals("page", moduleContextParameters.get("content.type"));
        assertEquals("plugin:foo", moduleContextParameters.get("content.plugin"));
    }
}
