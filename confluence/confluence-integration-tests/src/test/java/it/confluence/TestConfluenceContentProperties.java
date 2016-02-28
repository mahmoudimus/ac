package it.confluence;

import com.atlassian.confluence.api.model.JsonString;
import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentRepresentation;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.content.JsonContentProperty;
import com.atlassian.confluence.api.model.content.Space;
import com.atlassian.confluence.api.model.longtasks.LongTaskStatus;
import com.atlassian.confluence.api.model.longtasks.LongTaskSubmission;
import com.atlassian.confluence.api.model.pagination.PageResponse;
import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexFieldType;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UISupportValueType;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean.newContentPropertyModuleBean;
import static com.atlassian.plugin.connect.modules.beans.UISupportBean.newUISupportModuleBean;
import static com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexExtractionConfigurationBean.newContentPropertyIndexExtractionConfigurationBean;
import static com.google.common.collect.Lists.newArrayList;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Test for the Confluence ContentProperty module, utilizes the confluence rest client
 *
 * Must be run with -DtestedProduct=confluence
 */
public class TestConfluenceContentProperties {
    private static final Logger log = getLogger(TestConfluenceContentProperties.class);

    private static final String PROPERTY_KEY = "basepropkey";
    private static final String TEXT_FIELD_OBJECT_KEY = "mytitle";
    private static final String NUMERIC_FIELD_OBJECT_KEY = "likes";
    private static final String DATE_FIELD_OBJECT_KEY = "editTime";
    private static final String STRING_FIELD_OBJECT_KEY = "tags";
    private static final String STRING_FIELD_OBJECT_ALIAS_KEY = "category";
    private static final String NUMERIC_FIELD_OBJECT_ALIAS_KEY = "rank";

    // values
    private static final int NUMERIC_VALUE = 5;
    private static final String TEXT_FRAGMENT_VALUE = "searchasdf";
    private static final DateTime DATE_VALUE = new DateTime().withDate(2001, 1, 1).withTimeAtStartOfDay();
    private static final String STRING_VALUE = "stringToMatch";
    private static final String ALT_STRING_VALUE = "differentValue";
    private static final String STRING_VALUE_FOR_ALIAS = "knowledge";
    private static final String ALT_STRING_VALUE_FOR_ALIAS = "people";
    private static final int NUMERIC_VALUE_FOR_ALIAS = 1;
    private static final int ALT_NUMERIC_VALUE_FOR_ALIAS = 2;

    private static String baseUrl;
    private static List<Exception> setupFailure = new ArrayList<>();
    private static ConnectRunner runner;
    private static ConfluenceTestedProduct product = new ConfluenceTestedProductAccessor().getConfluenceProduct();
    private static ConfluenceRestClient restClient = new ConfluenceRestClient(product, "admin", "admin");

    private Promise<Content> contentToFind;
    private Promise<Content> contentWithOtherProperty;

    private Space space;
    private int spaceCount = 0;

    @BeforeClass
    public static void initRunner() throws Exception {

        try {
            baseUrl = new ConfluenceTestedProductAccessor().getConfluenceProduct().getProductInstance().getBaseUrl();

            ContentPropertyModuleBean moduleBean = newContentPropertyModuleBean()
                    .withKey("content-prop-module-key")
                    .withName(new I18nProperty("My Content Property Indexing module", null))
                    .withKeyConfiguration(
                            new ContentPropertyIndexKeyConfigurationBean(PROPERTY_KEY,
                                    newArrayList(
                                            newContentPropertyIndexExtractionConfigurationBean()
                                                    .withObjectName(TEXT_FIELD_OBJECT_KEY)
                                                    .withType(ContentPropertyIndexFieldType.text)
                                                    .build(),
                                            newContentPropertyIndexExtractionConfigurationBean()
                                                    .withObjectName(NUMERIC_FIELD_OBJECT_ALIAS_KEY)
                                                    .withType(ContentPropertyIndexFieldType.number)
                                                    .withAlias(NUMERIC_FIELD_OBJECT_ALIAS_KEY)
                                                    .withUiSupport(newUISupportModuleBean()
                                                            .withName(new I18nProperty("rank", "value"))
                                                            .withDataUri("/rest/test/rank")
                                                            .withDefaultOperator("=")
                                                            .withValueType(UISupportValueType.NUMBER)
                                                            .build())
                                                    .build(),
                                            newContentPropertyIndexExtractionConfigurationBean()
                                                    .withObjectName(DATE_FIELD_OBJECT_KEY)
                                                    .withType(ContentPropertyIndexFieldType.date)
                                                    .build(),
                                            newContentPropertyIndexExtractionConfigurationBean()
                                                    .withObjectName(NUMERIC_FIELD_OBJECT_KEY)
                                                    .withType(ContentPropertyIndexFieldType.number)
                                                    .build(),
                                            newContentPropertyIndexExtractionConfigurationBean()
                                                    .withObjectName(STRING_FIELD_OBJECT_KEY)
                                                    .withType(ContentPropertyIndexFieldType.string)
                                                    .build(),
                                            newContentPropertyIndexExtractionConfigurationBean()
                                                    .withObjectName(STRING_FIELD_OBJECT_ALIAS_KEY)
                                                    .withType(ContentPropertyIndexFieldType.string)
                                                    .withAlias(STRING_FIELD_OBJECT_ALIAS_KEY)
                                                    .withUiSupport(newUISupportModuleBean()
                                                            .withName(new I18nProperty("category", "value"))
                                                            .withDataUri("/rest/test/category")
                                                            .withDefaultOperator("=")
                                                            .withValueType(UISupportValueType.STRING)
                                                            .build())
                                                    .build()
                                    )))
                    .build();

            assertFalse("Key configurations should not be empty", moduleBean.getKeyConfigurations().isEmpty());

            runner = new ConnectRunner(baseUrl, AddonTestUtils.randomAddonKey())
                    .setAuthenticationToNone()
                    .addModules("confluenceContentProperties", moduleBean)
                    .start();
        } catch (Exception ex) {
            // avoid failing to init class, we'll rethrow this in setUp when it can be reported on properly
            setupFailure.add(ex);
        }
    }

    @Before
    public void setUp() throws Exception {
        if (!setupFailure.isEmpty())
            throw setupFailure.get(0);
        setupData();
    }

    @SuppressWarnings("unchecked")
    private void setupData() throws Exception {
        String spaceKey = "PROPTEST" + spaceCount++;
        space = restClient.spaces().create(Space.builder().key(spaceKey).name("Content property Test Space").build(), false).get();
        contentToFind = restClient.content().create(Content.builder(ContentType.PAGE)
                .space(space)
                .body("<p>Page content</p>", ContentRepresentation.STORAGE)
                .title("Page to find")
                .build());

        contentWithOtherProperty = restClient.content().create(Content.builder(ContentType.PAGE)
                .space(space)
                .body("<p>Dont find this one</p>", ContentRepresentation.STORAGE)
                .title("Page with different property")
                .build());

        Promise<Content> contentWithoutProperty = restClient.content().create(Content.builder(ContentType.PAGE)
                .space(space)
                .body("<p>Dont find this one</p>", ContentRepresentation.STORAGE)
                .title("Page without properties")
                .build());

        JsonObject propertyValue = new JsonObject();

        propertyValue.add(TEXT_FIELD_OBJECT_KEY, new JsonPrimitive("Sample  text to " + TEXT_FRAGMENT_VALUE));
        propertyValue.add(STRING_FIELD_OBJECT_KEY, new JsonPrimitive(STRING_VALUE));
        propertyValue.add(NUMERIC_FIELD_OBJECT_KEY, new JsonPrimitive(NUMERIC_VALUE));
        propertyValue.add(DATE_FIELD_OBJECT_KEY, new JsonPrimitive(DATE_VALUE.toString(ISODateTimeFormat.dateTime())));
        propertyValue.add(STRING_FIELD_OBJECT_ALIAS_KEY, new JsonPrimitive(STRING_VALUE_FOR_ALIAS));
        propertyValue.add(NUMERIC_FIELD_OBJECT_ALIAS_KEY, new JsonPrimitive(NUMERIC_VALUE_FOR_ALIAS));

        JsonContentProperty contentProperty = JsonContentProperty.builder()
                .content(contentToFind.get())
                .key(PROPERTY_KEY)
                .value(new JsonString(propertyValue.toString()))
                .build();

        Promise<JsonContentProperty> prop = restClient.contentProperties().create(contentProperty);

        JsonObject otherProperty = new JsonObject();
        otherProperty.add(TEXT_FIELD_OBJECT_KEY, new JsonPrimitive("Other text"));
        otherProperty.add(STRING_FIELD_OBJECT_KEY, new JsonPrimitive(ALT_STRING_VALUE));
        otherProperty.add(NUMERIC_FIELD_OBJECT_KEY, new JsonPrimitive(1));
        otherProperty.add(DATE_FIELD_OBJECT_KEY, new JsonPrimitive(new DateTime().toString(ISODateTimeFormat.dateTime())));
        otherProperty.add(STRING_FIELD_OBJECT_ALIAS_KEY, new JsonPrimitive(ALT_STRING_VALUE_FOR_ALIAS));
        otherProperty.add(NUMERIC_FIELD_OBJECT_ALIAS_KEY, new JsonPrimitive(ALT_NUMERIC_VALUE_FOR_ALIAS));

        Promise<JsonContentProperty> otherProp = restClient.contentProperties().create(JsonContentProperty.builder()
                .content(contentWithOtherProperty.get())
                .key(PROPERTY_KEY)
                .value(new JsonString(otherProperty.toString()))
                .build());

        // wait for property creation to finish
        Promises.when(prop, otherProp, contentWithoutProperty).get();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (runner != null) {
            runner.stopAndUninstall();
        }
    }

    @After
    public void tearDown() throws Exception {
        Promise<LongTaskSubmission> task = restClient.spaces().delete(Space.builder().key(space.getKey()).build());

        // this should be moved into RemoteLongTaskService
        Option<LongTaskStatus> longTaskStatus = restClient.longTasks().get(task.get().getId()).get();

        final int waitTime = 50;
        final int retry = 100;
        for (int i = 0; longTaskStatus.get().getPercentageComplete() < 100; i++) {
            Thread.sleep(50); // wait for the space deletion to finish
            longTaskStatus = restClient.longTasks().get(task.get().getId()).get();
            if (i > 100)
                fail("Delete space long task has not yet completed after " + waitTime * retry);
        }
    }

    @Test
    public void testTextContentProperty() throws Exception {
        PageResponse<Content> response = executeCql(String.format("content.property[%s].%s ~ %s", PROPERTY_KEY, TEXT_FIELD_OBJECT_KEY, TEXT_FRAGMENT_VALUE));
        assertHasOneMatchingItem(response, contentToFind);

        response = executeCql(String.format("content.property[%s].%s ~ %s", PROPERTY_KEY, TEXT_FIELD_OBJECT_KEY, "other"));
        assertHasOneMatchingItem(response, contentWithOtherProperty);
    }

    @Test
    public void testNumericContentProperty() throws Exception {
        PageResponse<Content> response = executeCql(String.format("content.property[%s].%s >= %s", PROPERTY_KEY, NUMERIC_FIELD_OBJECT_KEY, NUMERIC_VALUE));
        assertHasOneMatchingItem(response, contentToFind);

        response = executeCql(String.format("content.property[%s].%s < %s", PROPERTY_KEY, NUMERIC_FIELD_OBJECT_KEY, NUMERIC_VALUE));
        assertHasOneMatchingItem(response, contentWithOtherProperty);
    }

    @Test
    public void testStringContentProperty() throws Exception {
        PageResponse<Content> response = executeCql(String.format("content.property[%s].%s = %s", PROPERTY_KEY, STRING_FIELD_OBJECT_KEY, STRING_VALUE));
        assertHasOneMatchingItem(response, contentToFind);

        response = executeCql(String.format("content.property[%s].%s = %s", PROPERTY_KEY, STRING_FIELD_OBJECT_KEY, ALT_STRING_VALUE));
        assertHasOneMatchingItem(response, contentWithOtherProperty);
    }

    @Test
    public void testDateContentProperty() throws Exception {
        PageResponse<Content> response = executeCql(String.format("content.property[%s].%s < 2001-01-02", PROPERTY_KEY, DATE_FIELD_OBJECT_KEY));
        assertHasOneMatchingItem(response, contentToFind);

        response = executeCql(String.format("content.property[%s].%s >= 2001-01-02", PROPERTY_KEY, DATE_FIELD_OBJECT_KEY));
        assertHasOneMatchingItem(response, contentWithOtherProperty);
    }

    @Test
    public void testStringContentPropertyWithAlias() throws Exception {
        PageResponse<Content> response = executeCql(String.format("%s = %s", STRING_FIELD_OBJECT_ALIAS_KEY, STRING_VALUE_FOR_ALIAS));
        assertHasOneMatchingItem(response, contentToFind);
    }

    @Test
    public void testNumericContentPropertyWithAlias() throws Exception {
        PageResponse<Content> response = executeCql(String.format("%s > %d", NUMERIC_FIELD_OBJECT_ALIAS_KEY, NUMERIC_VALUE_FOR_ALIAS));
        assertHasOneMatchingItem(response, contentWithOtherProperty);
    }

    private void assertHasOneMatchingItem(PageResponse<Content> response, Promise<Content> content) throws Exception {
        assertThat(response.getResults(), hasSize(1));
        assertThat(Iterables.first(response).get().getTitle(), is(content.get().getTitle()));
    }

    private PageResponse<Content> executeCql(String cql) throws Exception {
        log.debug(cql);
        final int retries = 100;
        final int waitTime = 100;
        try {
            // confluence's index queue flushes every 5 secs (see config of IndexQueueFlusher), we don't have a rest client method to wait on this indexing
            for (int i = 0; i < retries; i++) {
                PageResponse<Content> result = restClient.cqlSearch().searchContent(cql).get();
                if (result.size() >= 1)
                    return result;

                Thread.sleep(waitTime);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not execute :" + cql, ex);
        }
        fail(String.format("Did not find any results after %d secs for query string : %s", retries * waitTime / 1000, cql));
        return null;
    }
}
