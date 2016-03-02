package it.confluence.contenttype;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.validation.ValidationResult;
import com.atlassian.confluence.api.service.exceptions.ServiceException;
import com.atlassian.elasticsearch.shaded.google.common.collect.Sets;
import com.google.common.collect.Iterables;
import org.junit.Test;

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

        Content content = createContent(buildContent(CONTENT_TYPE_1, null, CONTENT_TITLE));

        assertThat(content.getType(), is(CONTENT_TYPE_1));
        assertThat(content.getTitle(), containsString(CONTENT_TITLE));
        assertThat(content.getVersion().getNumber(), is(1));
    }

    @Test
    public void testCanRestrictContainerType() throws Exception {
        startConnectAddon(createBeanWithRestriction(TYPE_KEY_1, TYPE_NAME_1, Sets.newHashSet("blogpost"), Sets.newHashSet()));

        Content blog = createContainerContent(ContentType.BLOG_POST);
        Content page = createContainerContent(ContentType.PAGE);

        // Should allow to create under blog
        Content createdContent1 = createContent(buildContent(CONTENT_TYPE_1, blog, CONTENT_TITLE));

        assertThat(createdContent1.getType(), is(CONTENT_TYPE_1));
        assertThat(createdContent1.getVersion().getNumber(), is(1));

        // Should not allow to create under page
        try {
            createContent(buildContent(CONTENT_TYPE_1, page, CONTENT_TITLE));
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
}
