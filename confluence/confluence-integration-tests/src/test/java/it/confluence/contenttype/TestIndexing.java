package it.confluence.contenttype;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.pagination.PageResponse;
import com.atlassian.confluence.api.model.pagination.SimplePageRequest;
import com.atlassian.confluence.api.model.search.SearchOptions;
import com.atlassian.confluence.api.model.search.SearchResult;
import com.atlassian.confluence.rest.api.model.ExpansionsParser;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestIndexing extends AbstractExtensibleContentTypeTest {
    private final String CONTENT_TITLE = "Test Extensible Type Title";

    @Test
    public void testCanSearchExtensibleContentType() throws Exception {
        startConnectAddon(
                createSimpleBean(TYPE_KEY_1, TYPE_NAME_1),
                createSimpleBean(TYPE_KEY_2, TYPE_NAME_2)
        );

        createContent(buildContent(CONTENT_TYPE_1, null, CONTENT_TITLE + " 123", "body1"));
        createContent(buildContent(CONTENT_TYPE_2, null, CONTENT_TITLE + " 456", "body2"));
        rpc.flushIndexQueue();

        // Test can find content1
        assertThat(Iterables.any(search("123"), titleContains("123")), is(true));
        assertThat(Iterables.any(search("body1"), titleContains("123")), is(true));

        // Test can find content2
        assertThat(Iterables.any(search("456"), titleContains("456")), is(true));
        assertThat(Iterables.any(search("body2"), titleContains("456")), is(true));
    }

    @Test
    public void testCanSearchContentProperty() throws Exception {
        startConnectAddon(
                createBeanWithContentPropertyIndexingSupport(TYPE_KEY_1, TYPE_NAME_1, true, "key1"),
                createBeanWithContentPropertyIndexingSupport(TYPE_KEY_2, TYPE_NAME_2, true, "key1")
        );

        Content content1 = createContent(buildContent(CONTENT_TYPE_1, null, CONTENT_TITLE + " aUniqueKeyForContent1", "body1"));
        createContentProperty(content1, "key1", "{\"content\": \"key1ForContent1\"}");
        createContentProperty(content1, "key2", "{\"content\": \"key2ForContent1\"}");

        Content content2 = createContent(buildContent(CONTENT_TYPE_2, null, CONTENT_TITLE + " aUniqueKeyForContent2", "body2"));
        createContentProperty(content2, "key1", "{\"content\": \"key1ForContent2\"}");
        createContentProperty(content2, "key2", "{\"content\": \"key2ForContent2\"}");

        rpc.flushIndexQueue();

        // Test can find content1
        assertThat(Iterables.any(search("key1ForContent1"), titleContains("aUniqueKeyForContent1")), is(true));
        assertThat(Iterables.any(search("key2ForContent1"), titleContains("aUniqueKeyForContent1")), is(false));

        // Test can find content2
        assertThat(Iterables.any(search("key1ForContent2"), titleContains("aUniqueKeyForContent2")), is(true));
        assertThat(Iterables.any(search("key2ForContent2"), titleContains("aUniqueKeyForContent2")), is(false));

        // Can still find content with body
        assertThat(Iterables.any(search("body1"), titleContains("aUniqueKeyForContent1")), is(true));
        assertThat(Iterables.any(search("body2"), titleContains("aUniqueKeyForContent2")), is(true));
    }

    @Test
    public void testCanDisableIndexing() throws Exception {
        startConnectAddon(
                createBeanWithContentPropertyIndexingSupport(TYPE_KEY_1, TYPE_NAME_1, true, "key1"),
                createBeanWithContentPropertyIndexingSupport(TYPE_KEY_2, TYPE_NAME_2, false, "key2")
        );

        createContent(buildContent(CONTENT_TYPE_1, null, CONTENT_TITLE + " indexingEnabled", "body1"));
        createContent(buildContent(CONTENT_TYPE_2, null, CONTENT_TITLE + " indexingDisabled", "body2"));

        rpc.flushIndexQueue();


        assertThat(Iterables.any(search("body1"), titleContains("indexingEnabled")), is(true));
        assertThat(Iterables.any(search("body2"), titleContains("indexingDisabled")), is(false));
    }

    private PageResponse<SearchResult> search(String searchString) {
        return restClient
                .cqlSearch()
                .search("siteSearch ~ \"" + searchString + "\"", SearchOptions.buildDefault(), new SimplePageRequest(0, 500), ExpansionsParser.parse(""))
                .claim();
    }

    private Predicate<SearchResult> titleContains(String title) {
        return searchResult -> searchResult.getTitle().contains(title);
    }
}
