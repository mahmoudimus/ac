package com.atlassian.plugin.connect.plugin.module.confluence.context.extractor;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.plugin.connect.plugin.module.confluence.context.serializer.ContentSerializer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ContentContextMapParameterExtractorTest
{
    @Mock private ContentSerializer contentSerializer;

    @InjectMocks ContentContextMapParameterExtractor extractor;

    @Test
    public void testInvalidContentInContext()
    {
        Optional<ContentEntityObject> result = extractor.extract(ImmutableMap.<String, Object>of(
            "content", "yup"
        ));

        assertFalse(result.isPresent());
    }

    @Test
    public void testValidContentInContext()
    {
        ContentEntityObject content = mock(ContentEntityObject.class);
        Optional<ContentEntityObject> result = extractor.extract(ImmutableMap.<String, Object>of(
            "content", content
        ));

        assertTrue(result.isPresent());
        assertSame(content, result.get());
    }
}
