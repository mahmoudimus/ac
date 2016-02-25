package it.confluence.contenttype;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.pagination.PageResponse;
import com.atlassian.confluence.api.model.pagination.SimplePageRequest;
import com.atlassian.confluence.api.model.validation.ValidationResult;
import com.atlassian.confluence.api.service.exceptions.ServiceException;
import com.atlassian.confluence.rest.api.model.ExpansionsParser;
import com.atlassian.elasticsearch.shaded.google.common.collect.Lists;
import com.atlassian.elasticsearch.shaded.google.common.collect.Sets;
import com.atlassian.util.concurrent.Promise;
import com.google.common.collect.Iterables;
import org.junit.Ignore;
import org.junit.Test;

import static it.confluence.ConfluenceWebDriverTestBase.TestSpace.DEMO;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.fail;

public class TestExtensibleContentType extends AbstractExtensibleContentTypeTest {
    private final String CONTENT_TITLE = "Test Extensible Type Title";

    @Test
    public void testCanInstallExtensibleContentType() throws Exception {
        startConnectAddon(createSimpleBean(TYPE_KEY_1, TYPE_NAME_1));

        checkExtensibleContentType(TYPE_KEY_1, TYPE_NAME_1);
    }

    @Test
    public void testCanCreateExtensibleContentType() throws Exception {
        startConnectAddon(createSimpleBean(TYPE_KEY_1, TYPE_NAME_1));

        Content content = createContent(buildContent(contentType1, null, CONTENT_TITLE));

        assertThat(content.getType(), is(contentType1));
        assertThat(content.getTitle(), containsString(CONTENT_TITLE));
        assertThat(content.getVersion().getNumber(), is(1));
    }

    @Test
    public void testCanRestrictContainerType() throws Exception {
        startConnectAddon(createBeanWithRestriction(TYPE_KEY_1, TYPE_NAME_1, Sets.newHashSet("blogpost"), Sets.newHashSet()));

        Content blog = createContainerContent(ContentType.BLOG_POST);
        Content page = createContainerContent(ContentType.PAGE);

        // Should allow to create under blog
        Content createdContent1 = createContent(buildContent(contentType1, blog, CONTENT_TITLE));

        assertThat(createdContent1.getType(), is(contentType1));
        assertThat(createdContent1.getVersion().getNumber(), is(1));

        // Should not allow to create under page
        try {
            createContent(buildContent(contentType1, page, CONTENT_TITLE));
            fail("Should not allow to create under page");
        } catch (ServiceException e) {
            // Expected
            ValidationResult result = e.getOptionalValidationResult().getOrNull();
            assertThat(result, notNullValue());
            assertThat(result.isValid(), is(false));
            assertThat(Iterables.any(result.getErrors(), error ->
                    error.getMessage().toString().contains("page is not a supported container type")), is(true));
        }
    }

    @Test
    @Ignore("Blocked by expansion not support dash/colon character. Will address later")
    public void testCanRestrictContainedType() throws Exception {
        startConnectAddon(
                createBeanWithRestriction(TYPE_KEY_1, TYPE_NAME_1, Sets.newHashSet("global"), Sets.newHashSet(getCompleteContentTypeKey(TYPE_KEY_2))),
                createBeanWithRestriction(TYPE_KEY_2, TYPE_NAME_2, Sets.newHashSet("global", getCompleteContentTypeKey(TYPE_KEY_1)), Sets.newHashSet())
        );

        Content content1 = createContent(buildContent(contentType1, null, CONTENT_TITLE));
        Content content2 = createContent(
                Content.builder()
                        .type(contentType2)
                        .space(DEMO.getKey())
                        .title("Child of " + content1.getTitle() + " " + System.currentTimeMillis())
                        .ancestors(Lists.newArrayList(content1))
                        .build());

        assertThat(content1.getType(), is(contentType1));
        assertThat(content2.getType(), is(contentType2));

        Promise<PageResponse<Content>> children = restClient.content().getChildren(content1, new SimplePageRequest(0, Integer.MAX_VALUE), ExpansionsParser.parse(""));
        PageResponse<Content> contents = children.get();

        assertThat(contents, notNullValue());
    }
}
