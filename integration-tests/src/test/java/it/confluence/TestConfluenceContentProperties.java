package it.confluence;

import com.atlassian.confluence.api.model.JsonString;
import com.atlassian.confluence.api.model.content.*;
import com.atlassian.confluence.api.model.longtasks.LongTaskStatus;
import com.atlassian.confluence.api.model.longtasks.LongTaskSubmission;
import com.atlassian.confluence.api.model.pagination.PageResponse;
import com.atlassian.confluence.rest.client.*;
import com.atlassian.confluence.rest.client.authentication.AuthenticatedWebResourceProvider;
import com.atlassian.confluence.rest.client.impl.RemoteLongTaskServiceImpl;
import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexFieldType;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.AbstractBrowserlessTest;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean.newContentPropertyModuleBean;
import static com.google.common.collect.Lists.newArrayList;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for the Confluence ContentProperty module, utilizes the confluence rest client
 *
 * Must be run with -DtestedProduct=confluence
 */
public class TestConfluenceContentProperties extends AbstractBrowserlessTest
{
    private static final Logger log = LoggerFactory.getLogger(TestConfluenceContentProperties.class);

    public static final String PROPERTY_KEY = "basepropkey";
    public static final String TEXT_FIELD_OBJECT_KEY = "mytitle";
    public static final String NUMERIC_FIELD_OBJECT_KEY = "likes";
    public static final String DATE_FIELD_OBJECT_KEY = "editTime";
    public static final String STRING_FIELD_OBJECT_KEY = "tags";

    // values
    private static final int NUMERIC_VALUE = 5;
    private static final String TEXT_FRAGMENT_VALUE = "searchasdf";
    private static final DateTime DATE_VALUE = new DateTime().withDate(2001, 1, 1).withTimeAtStartOfDay();
    private static final String STRING_VALUE = "stringToMatch";
    private static final String ALT_STRING_VALUE = "differentValue";

    private static String baseUrl;
    private static List<Exception> setupFailure = new ArrayList<Exception>();
    private static ListeningExecutorService executor;

    private RemoteContentService contentService;
    private RemoteSpaceService spaceService;
    private RemoteContentPropertyService contentPropertyService;
    private RemoteLongTaskService longTaskService;
    private RemoteCQLSearchService cqlSearchService;

    private Promise<Content> contentToFind;
    private Promise<Content> contentWithOtherProperty;

    private Space space;
    private int spaceCount = 0;

    @BeforeClass
    public static void initRunner() throws Exception
    {
        try
        {
            baseUrl = product.getProductInstance().getBaseUrl();
            assertTrue("Should be running with confluence (set -DtestedProduct=confluence), instead found baseUrl " + baseUrl,
                    baseUrl.contains("confluence"));

            ContentPropertyModuleBean moduleBean = newContentPropertyModuleBean()
                .withKey("content-prop-module-key")
                .withName(new I18nProperty("My Content Property Indexing module", "my.18n.name"))
                .withKeyConfiguration(
                        new ContentPropertyIndexKeyConfigurationBean(PROPERTY_KEY,
                                newArrayList(
                                        new ContentPropertyIndexExtractionConfigurationBean(TEXT_FIELD_OBJECT_KEY, ContentPropertyIndexFieldType.text),
                                        new ContentPropertyIndexExtractionConfigurationBean(NUMERIC_FIELD_OBJECT_KEY, ContentPropertyIndexFieldType.number),
                                        new ContentPropertyIndexExtractionConfigurationBean(DATE_FIELD_OBJECT_KEY, ContentPropertyIndexFieldType.date),
                                        new ContentPropertyIndexExtractionConfigurationBean(STRING_FIELD_OBJECT_KEY, ContentPropertyIndexFieldType.string))))
                    .build();

            assertFalse("Key configurations should not be empty", moduleBean.getKeyConfigurations().isEmpty());

            System.out.println("Installing connect module to : "+baseUrl);

            new ConnectRunner(baseUrl, AddonTestUtils.randomAddOnKey())
                    .setAuthenticationToNone()
                    .addModules("confluenceContentProperties", moduleBean)
                    .start();

            executor = createExecutor();
        }
        catch (Exception ex)
        {
            // avoid failing to init class, we'll rethrow this in setUp when it can be reported on properly
            setupFailure.add(ex);
        }
    }

    @Before
    public void setUp() throws Exception
    {
        if (!setupFailure.isEmpty())
            throw setupFailure.get(0);
        initConfluenceClient();
        setupData();
    }

    private static ListeningExecutorService createExecutor()
    {
        return MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3)); // approx 20% faster
//        return MoreExecutors.sameThreadExecutor();
    }

    private void initConfluenceClient()
    {
        AuthenticatedWebResourceProvider authenticatedWebResourceProvider = new AuthenticatedWebResourceProvider(ConfluenceRestClientFactory.newClient(), baseUrl, "");
        authenticatedWebResourceProvider.setAuthContext("admin", "admin".toCharArray());

        contentService = new RemoteContentServiceImpl(authenticatedWebResourceProvider, executor);
        spaceService = new RemoteSpaceServiceImpl(authenticatedWebResourceProvider, executor);
        contentPropertyService = new RemoteContentPropertyServiceImpl(authenticatedWebResourceProvider, executor);
        cqlSearchService = new RemoteCQLSearchServiceImpl(authenticatedWebResourceProvider, executor);
        longTaskService = new RemoteLongTaskServiceImpl(authenticatedWebResourceProvider, executor);
    }

    private void setupData() throws Exception
    {
        String spaceKey = "PROPTEST"+spaceCount++;
        space = spaceService.create(Space.builder().key(spaceKey).name("Content property Test Space").build(), false).get();
        contentToFind = contentService.create(Content.builder(ContentType.PAGE)
                .space(space)
                .body("<p>Page content</p>", ContentRepresentation.STORAGE)
                .title("Page to find")
                .build());

        contentWithOtherProperty = contentService.create(Content.builder(ContentType.PAGE)
                .space(space)
                .body("<p>Dont find this one</p>", ContentRepresentation.STORAGE)
                .title("Page with different property")
                .build());

        Promise<Content> contentWithoutProperty = contentService.create(Content.builder(ContentType.PAGE)
                .space(space)
                .body("<p>Dont find this one</p>", ContentRepresentation.STORAGE)
                .title("Page without properties")
                .build());

        JsonObject propertyValue = new JsonObject();

        propertyValue.add(TEXT_FIELD_OBJECT_KEY, new JsonPrimitive("Sample  text to "+TEXT_FRAGMENT_VALUE));
        propertyValue.add(STRING_FIELD_OBJECT_KEY, new JsonPrimitive(STRING_VALUE));
        propertyValue.add(NUMERIC_FIELD_OBJECT_KEY, new JsonPrimitive(NUMERIC_VALUE));
        propertyValue.add(DATE_FIELD_OBJECT_KEY, new JsonPrimitive(DATE_VALUE.toString(ISODateTimeFormat.dateTime())));

        JsonContentProperty contentProperty = JsonContentProperty.builder()
                .content(contentToFind.get())
                .key(PROPERTY_KEY)
                .value(new JsonString(propertyValue.toString()))
                .build();

        System.out.println(contentProperty.getValue());

        Promise<JsonContentProperty> prop = contentPropertyService.create(contentProperty);

        JsonObject otherProperty = new JsonObject();
        otherProperty.add(TEXT_FIELD_OBJECT_KEY, new JsonPrimitive("Other text"));
        otherProperty.add(STRING_FIELD_OBJECT_KEY, new JsonPrimitive(ALT_STRING_VALUE));
        otherProperty.add(NUMERIC_FIELD_OBJECT_KEY, new JsonPrimitive(1));
        otherProperty.add(DATE_FIELD_OBJECT_KEY, new JsonPrimitive(new DateTime().toString(ISODateTimeFormat.dateTime())));

        Promise<JsonContentProperty> otherProp = contentPropertyService.create(JsonContentProperty.builder()
                .content(contentWithOtherProperty.get())
                .key(PROPERTY_KEY)
                .value(new JsonString(otherProperty.toString()))
                .build());

        // wait for property creation to finish
        Promises.when(prop, otherProp, contentWithoutProperty).get();
    }

    @AfterClass
    public static void tearDownClass()
    {
        executor.shutdown();
    }

    @After
    public void tearDown() throws Exception
    {
        Promise<LongTaskSubmission> task = spaceService.delete(Space.builder().key(space.getKey()).build());

        // this should be moved into RemoteLongTaskService
        Option<LongTaskStatus> longTaskStatus = longTaskService.get(task.get().getId()).get();

        final int waitTime = 50;
        final int retry = 100;
        for (int i = 0 ; longTaskStatus.get().getPercentageComplete() < 100; i++)
        {
            Thread.sleep(50); // wait for the space deletion to finish
            longTaskStatus = longTaskService.get(task.get().getId()).get();
            if (i > 100)
                fail("Delete space long task has not yet completed after " + waitTime * retry);
        }
    }

    @Test
    public void testTextContentProperty() throws Exception
    {
        PageResponse<Content> response = executeCql(String.format("content.property[%s].%s ~ %s", PROPERTY_KEY, TEXT_FIELD_OBJECT_KEY, TEXT_FRAGMENT_VALUE));
        assertHasOneMatchingItem(response, contentToFind);

        response = executeCql(String.format("content.property[%s].%s ~ %s", PROPERTY_KEY, TEXT_FIELD_OBJECT_KEY, "other"));
        assertHasOneMatchingItem(response, contentWithOtherProperty);
    }

    @Test
    public void testNumericContentProperty() throws Exception
    {
        PageResponse<Content> response = executeCql(String.format("content.property[%s].%s >= %s", PROPERTY_KEY, NUMERIC_FIELD_OBJECT_KEY, NUMERIC_VALUE));
        assertHasOneMatchingItem(response, contentToFind);

        response = executeCql(String.format("content.property[%s].%s < %s", PROPERTY_KEY, NUMERIC_FIELD_OBJECT_KEY, NUMERIC_VALUE));
        assertHasOneMatchingItem(response ,contentWithOtherProperty);
    }

    @Test
    public void testStringContentProperty() throws Exception
    {
        PageResponse<Content> response = executeCql(String.format("content.property[%s].%s = %s", PROPERTY_KEY, STRING_FIELD_OBJECT_KEY, STRING_VALUE));
        assertHasOneMatchingItem(response, contentToFind);

        response = executeCql(String.format("content.property[%s].%s = %s", PROPERTY_KEY, STRING_FIELD_OBJECT_KEY, ALT_STRING_VALUE));
        assertHasOneMatchingItem(response, contentWithOtherProperty);
    }

    @Test
    public void testDateContentProperty() throws Exception
    {
        PageResponse<Content> response = executeCql(String.format("content.property[%s].%s < 2001-01-02", PROPERTY_KEY, DATE_FIELD_OBJECT_KEY));
        assertHasOneMatchingItem(response, contentToFind);

        response = executeCql(String.format("content.property[%s].%s >= 2001-01-02", PROPERTY_KEY, DATE_FIELD_OBJECT_KEY));
        assertHasOneMatchingItem(response, contentWithOtherProperty);
    }

    private void assertHasOneMatchingItem(PageResponse<Content> response, Promise<Content> content) throws Exception
    {
        assertThat(response.getResults(), hasSize(1));
        assertThat(Iterables.first(response).get().getTitle(), is(content.get().getTitle()));
    }

    private PageResponse<Content> executeCql(String cql) throws Exception
    {
        log.debug(cql);
        try
        {
            // confluence's index queue flushes every 5 secs (see config of IndexQueueFlusher), we don't have a rest client method to wait on this indexing
            for (int i = 0; i < 60; i++)
            {
                PageResponse<Content> result = cqlSearchService.searchContent(cql).get();
                if (result.size() >= 1)
                    return result;

                Thread.sleep(100);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Could not execute :"+cql,ex);
        }
        fail("Did not find any results after 6 secs for query string : " + cql);
        return null;
    }
}
