package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.model.JsonString;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.content.JsonContentProperty;
import com.atlassian.confluence.api.model.content.id.ContentId;
import com.atlassian.confluence.api.service.content.ContentPropertyService;
import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.confluence.ConfluenceFeatureManager;

import com.google.common.base.Splitter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentPropertyExtractorTest
{
    private final ContentType extensible = ContentType.valueOf("extensible");

    private StringBuffer searchableText;
    private ContentPropertyExtractor extractor;
    @Mock private CustomContentEntityObject customContentEntityObject;
    @Mock private ContentPropertyService.ContentPropertyFinder contentPropertyFinder;
    @Mock private ConfluenceFeatureManager confluenceFeatureManager;
    @Mock private ContentPropertyService contentPropertyService;

    @Before
    public void setup()
    {
        searchableText = new StringBuffer();
        extractor = new ContentPropertyExtractor(confluenceFeatureManager, contentPropertyService, "testProperty");

        when(customContentEntityObject.getContentId()).thenReturn(ContentId.of(extensible, 1));
        when(contentPropertyService.find(anyVararg())).thenReturn(contentPropertyFinder);
        when(contentPropertyFinder.withContentId(any())).thenReturn(contentPropertyFinder);
        when(contentPropertyFinder.withPropertyKey(any())).thenReturn(contentPropertyFinder);
    }

    @Test
    public void shouldAppendContentPropertyToSearchableText()
    {
        JsonContentProperty property = JsonContentProperty
                .builder()
                .key("testProperty")
                .value(new JsonString("contentPropertyValue"))
                .build();

        when(contentPropertyFinder.fetchOne()).thenReturn(Option.some(property));

        extractor.addContentPropertyToSearchableText(customContentEntityObject, searchableText);

        Iterable<String> strings = Splitter.on(" ").split(searchableText);
        Assert.assertThat(strings, hasItem("contentPropertyValue"));
    }

    @Test
    public void shouldNotAppendIfContentPropertyNotAvailable()
    {
        when(contentPropertyFinder.fetchOne()).thenReturn(Option.none());

        extractor.addContentPropertyToSearchableText(customContentEntityObject, searchableText);

        Assert.assertThat(searchableText.toString(), is(""));
    }
}
