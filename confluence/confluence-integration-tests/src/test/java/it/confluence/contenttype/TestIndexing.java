package it.confluence.contenttype;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.google.common.collect.Iterables;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestIndexing extends AbstractExtensibleContentTypeTest {
    private static TestUser user;
    private final String CONTENT_TITLE = "Test Extensible Type Title";

    @BeforeClass
    public static void setup() {
        user = testUserFactory.basicUser();
    }

    @Test
    public void testCanSearchExtensibleContentType() throws Exception {
        startConnectAddon(
                createSimpleBean(TYPE_KEY_1, TYPE_NAME_1),
                createSimpleBean(TYPE_KEY_2, TYPE_NAME_2)
        );

        createContent(buildContent(contentType1, null, CONTENT_TITLE + " 123", "body1"));
        createContent(buildContent(contentType2, null, CONTENT_TITLE + " 456", "body2"));
        rpc.flushIndexQueue();

        login(user);

        // Test can find content1
        List<PageElement> results1 = product.visit(SearchPage.class).searchQuery("123").results();
        assertThat(Iterables.any(results1, result -> result.getText().contains("123")), is(true));
        List<PageElement> results3 = product.visit(SearchPage.class).searchQuery("body1").results();
        assertThat(Iterables.any(results3, result -> result.getText().contains("123")), is(true));

        // Test can find content2
        List<PageElement> results2 = product.visit(SearchPage.class).searchQuery("456").results();
        assertThat(Iterables.any(results2, result -> result.getText().contains("456")), is(true));
        List<PageElement> results4 = product.visit(SearchPage.class).searchQuery("body2").results();
        assertThat(Iterables.any(results4, result -> result.getText().contains("456")), is(true));
    }

    @Test
    public void testCanSearchContentProperty() throws Exception {
        startConnectAddon(
                createBeanWithContentPropertyIndexingSupport(TYPE_KEY_1, TYPE_NAME_1, true, "key1"),
                createBeanWithContentPropertyIndexingSupport(TYPE_KEY_2, TYPE_NAME_2, true, "key1")
        );

        Content content1 = createContent(buildContent(contentType1, null, CONTENT_TITLE + " aUniqueKeyForContent1", "body1"));
        createContentProperty(content1, "key1", "{\"content\": \"key1ForContent1\"}");
        createContentProperty(content1, "key2", "{\"content\": \"key2ForContent1\"}");

        Content content2 = createContent(buildContent(contentType2, null, CONTENT_TITLE + " aUniqueKeyForContent2", "body2"));
        createContentProperty(content2, "key1", "{\"content\": \"key1ForContent2\"}");
        createContentProperty(content2, "key2", "{\"content\": \"key2ForContent2\"}");

        rpc.flushIndexQueue();

        login(user);

        // Test can find content1
        List<PageElement> results1 = product.visit(SearchPage.class).searchQuery("key1ForContent1").results();
        assertThat(Iterables.any(results1, result -> result.getText().contains("aUniqueKeyForContent1")), is(true));
        assertThat(product.visit(SearchPage.class).searchQueryWithOutWaiting("key2ForContent1").noResult(), is(true));

        // Test can find content2
        List<PageElement> results2 = product.visit(SearchPage.class).searchQuery("key1ForContent2").results();
        assertThat(Iterables.any(results2, result -> result.getText().contains("aUniqueKeyForContent2")), is(true));
        assertThat(product.visit(SearchPage.class).searchQueryWithOutWaiting("key2ForContent2").noResult(), is(true));

        // Can still find content with body
        results1 = product.visit(SearchPage.class).searchQuery("body1").results();
        assertThat(Iterables.any(results1, result -> result.getText().contains("aUniqueKeyForContent1")), is(true));

        results2 = product.visit(SearchPage.class).searchQuery("body2").results();
        assertThat(Iterables.any(results2, result -> result.getText().contains("aUniqueKeyForContent2")), is(true));
    }

    @Test
    public void testCanDisableIndexing() throws Exception {
        startConnectAddon(
                createBeanWithContentPropertyIndexingSupport(TYPE_KEY_1, TYPE_NAME_1, true, "key1"),
                createBeanWithContentPropertyIndexingSupport(TYPE_KEY_2, TYPE_NAME_2, false, "key2")
        );

        createContent(buildContent(contentType1, null, CONTENT_TITLE + " indexingEnabled", "body1"));
        createContent(buildContent(contentType2, null, CONTENT_TITLE + " indexingDisabled", "body2"));

        login(user);

        List<PageElement> results1 = product.visit(SearchPage.class).searchQuery("body1").results();
        assertThat(Iterables.any(results1, result -> result.getText().contains("indexingEnabled")), is(true));

        assertThat(product.visit(SearchPage.class).searchQueryWithOutWaiting("body2").noResult(), is(true));
    }
}
