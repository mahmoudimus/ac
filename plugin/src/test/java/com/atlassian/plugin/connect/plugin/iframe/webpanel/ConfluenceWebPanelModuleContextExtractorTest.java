package com.atlassian.plugin.connect.plugin.iframe.webpanel;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.actions.AbstractPageAwareAction;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfluenceWebPanelModuleContextExtractorTest
{
    private ConfluenceWebPanelModuleContextExtractor extractor;

    @Before
    public void setup()
    {
        extractor = new ConfluenceWebPanelModuleContextExtractor();
    }

    @Test
    public void testExtractPageFromContext()
    {
        Page page = mock(Page.class);
        when(page.getId()).thenReturn(123L);

        ModuleContextParameters params = extractor.extractParameters(ImmutableMap.<String, Object>of("page", page));
        assertEquals("123", params.get("page.id"));
    }

    @Test
    public void testExtractSpaceFromContext()
    {
        Space space = mock(Space.class);
        when(space.getKey()).thenReturn("SPACE");
        when(space.getId()).thenReturn(321L);

        ModuleContextParameters params = extractor.extractParameters(ImmutableMap.<String, Object>of("space", space));
        assertEquals("321", params.get("space.id"));
        assertEquals("SPACE", params.get("space.key"));
    }

    @Test
    public void testExtractPageAndSpaceParametersFromContextWithAbstractPageAwareAction()
    {
        Space space = mock(Space.class);
        when(space.getKey()).thenReturn("SPACE");
        when(space.getId()).thenReturn(321L);

        AbstractPage page = mock(AbstractPage.class);
        when(page.getId()).thenReturn(123L);
        when(page.getSpace()).thenReturn(space);

        AbstractPageAwareAction pageAwareAction = mock(AbstractPageAwareAction.class);
        when(pageAwareAction.getPage()).thenReturn(page);

        ModuleContextParameters params = extractor.extractParameters(ImmutableMap.<String, Object>of("action", pageAwareAction));

        assertEquals("123", params.get("page.id"));
        assertEquals("321", params.get("space.id"));
        assertEquals("SPACE", params.get("space.key"));
    }
}
