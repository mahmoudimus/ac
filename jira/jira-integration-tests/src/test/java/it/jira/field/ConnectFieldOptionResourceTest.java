package it.jira.field;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Pair;
import com.atlassian.jira.rest.api.pagination.PageBean;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.jira.field.option.rest.ConnectFieldOptionBean;
import com.atlassian.plugin.connect.jira.field.option.rest.ReplaceRequestBean;
import com.atlassian.plugin.connect.jira.util.Json;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.product.TestedProductAccessor;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.codehaus.jackson.JsonNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.atlassian.fugue.Pair.pair;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ConnectFieldOptionResourceTest {
    private final Gson gson = new Gson();

    private String addonKey;
    private final String fieldKey = "singleSelectTestField";
    private final String baseUrl = TestedProductAccessor.get().getTestedProduct().getProductInstance().getBaseUrl();
    private String restPath;

    private InstallHandlerServlet installHandlerServlet;
    private ConnectRunner runner;

    @Before
    public void init() throws Exception {
        addonKey = "addon_" + RandomStringUtils.randomAlphabetic(10); // random key to achieve independent tests
        restPath = baseUrl + "/rest/atlassian-connect/1/jira/addon/" + addonKey + "/field/" + fieldKey + "/option/";

        installHandlerServlet = new InstallHandlerServlet();
        runner = new ConnectRunner(baseUrl, addonKey)
                .addJWT(installHandlerServlet)
                .addModules("jiraIssueFields", ConnectFieldModuleBean.newBuilder()
                        .withKey(fieldKey)
                        .withName(new I18nProperty("field name", null))
                        .withBaseType(ConnectFieldType.TEXT)
                        .build())
                .addScopes(ScopeName.READ)
                .start();
    }

    @After
    public void tearDown() throws Exception {
        runner.stopAndUninstall();
    }

    @Test
    public void addedOptionsCanBeRetrieved() throws Exception {
        createOption("5");
        createOption("6");
        assertThat(readOptions(), equalTo(ImmutableList.of(new ConnectFieldOptionBean(1, 5.0d), new ConnectFieldOptionBean(2, 6.0d))));
    }

    @Test
    public void testPagination() throws Exception {
        Stream.of("1", "2", "3", "4", "5").forEach(this::createOption);
        PageBean<ConnectFieldOptionBean> page1 = readOptions(0, 3);
        PageBean<ConnectFieldOptionBean> page2 = readOptions(3, 7);

        assertFalse(page1.getIsLast());
        assertTrue(page2.getIsLast());

        assertThat(page1.getValues(), equalTo(ImmutableList.of(new ConnectFieldOptionBean(1, 1.0d), new ConnectFieldOptionBean(2, 2.0d), new ConnectFieldOptionBean(3, 3.0d))));
        assertThat(page2.getValues(), equalTo(ImmutableList.of(new ConnectFieldOptionBean(4, 4.0d), new ConnectFieldOptionBean(5, 5.0d))));
    }

    @Test
    public void optionCanBeDeleted() throws Exception {
        createOption("5");
        int responseCode = establishConnection("1", HttpMethod.DELETE).getResponseCode();
        assertEquals(204, responseCode);
        assertThat(readOptions(), hasSize(0));
    }

    @Test
    public void optionValueCanBeReplacedInIssues() throws Exception {
        createOption("1");
        createOption("2");

        HttpURLConnection connection = sendObject(establishConnection("replace", HttpMethod.POST), new ReplaceRequestBean(1, 2));
        assertEquals(200, connection.getResponseCode());
    }


    @Test
    public void bothValuesAreRequiredInReplace() throws IOException {
        createOption("1");
        createOption("2");

        HttpURLConnection connection = sendObject(establishConnection("replace", HttpMethod.POST), new ReplaceRequestBean(null, 2));
        assertEquals(400, connection.getResponseCode());

        connection = sendObject(establishConnection("replace", HttpMethod.POST), new ReplaceRequestBean(1, null));
        assertEquals(400, connection.getResponseCode());
    }

    @Test
    public void newOptionCanBePutWithSpecifiedId() throws Exception {
        ConnectFieldOptionBean option = new ConnectFieldOptionBean(42, "1");

        putOption(42, option);

        assertEquals(ImmutableList.of(option), readOptions());
    }

    @Test
    public void optionCanBeUpdated() throws Exception {
        ConnectFieldOptionBean option = new ConnectFieldOptionBean(42, "1");
        ConnectFieldOptionBean updatedOption = new ConnectFieldOptionBean(42, "2");

        putOption(option);
        putOption(updatedOption);

        assertEquals(ImmutableList.of(updatedOption), readOptions());
    }

    @Test
    public void errorIsReturnedIfIdInPutIsInconsistent() throws Exception {
        ErrorCollection errorCollection = putOption(1, new ConnectFieldOptionBean(42, "42")).left().get();
        assertThat(errorCollection.getErrors(), hasEntry("id", "id should be equal to 1"));
    }

    @Test
    public void valueIsRequired() {
        ErrorCollection errorCollection = putOption(1, new ConnectFieldOptionBean(1, null)).left().get();
        assertThat(errorCollection.getErrors(), hasEntry("value", "value is required"));
    }

    @Test
    public void idIsRequiredForPut() {
        ErrorCollection errorCollection = putOption(1, new ConnectFieldOptionBean(null, "4")).left().get();
        assertThat(errorCollection.getErrors(), hasEntry("id", "id is required"));
    }

    @Test
    public void addOnHasAccessOnlyToItsOwnFields() {
        List<Triple<String, HttpMethod, Object>> methods = ImmutableList.of(
                Triple.of("", HttpMethod.GET, null),
                Triple.of("", HttpMethod.POST, new ConnectFieldOptionBean(null, "42")),
                Triple.of("3", HttpMethod.PUT, new ConnectFieldOptionBean(3, "42")),
                Triple.of("3", HttpMethod.DELETE, null),
                Triple.of("replace", HttpMethod.POST, new ReplaceRequestBean(2, 3)));

        List<Pair<String, String>> invalidPaths = ImmutableList.of(
                pair(baseUrl + "/rest/atlassian-connect/1/jira/addon/not_my_addon_key/field/" + fieldKey + "/option/", "Access denied (expected an authenticated add-on with key \"not_my_addon_key\" or a sysadmin)"),
                pair(baseUrl + "/rest/atlassian-connect/1/jira/addon/" + addonKey + "/field/not_my_field/option/", "Field with key \"not_my_field\" does not exists for the add-on \"" + addonKey + "\""));

        invalidPaths.forEach(pathAndError -> {
            restPath = pathAndError.left();
            methods.forEach(method -> {
                System.out.println("Testing " + method.getMiddle() + " " + restPath + method.getLeft() + " // " + method.getRight());
                HttpURLConnection connection = establishConnection(method.getLeft(), method.getMiddle());
                sendObject(connection, method.getRight());

                Either<ErrorCollection, Object> response = readOutput(connection, Object.class);
                assertThat(response.left().get().getErrorMessages(), hasItems(pathAndError.right()));
            });
        });
    }

    @Test
    public void differentJsonObjectsAreHandledProperly() throws Exception {
        List<String> differentTypes = ImmutableList.of(
                "1",
                "4.2",
                "false",
                "[1, 2, 3]",
                "\"string\"",
                "{ \"a\": \"42\"}");

        differentTypes.forEach(json -> {
            assertEquals(Json.parse(json).get().toString(), createOption(json).get("value").toString());
            assertEquals(Json.parse(json).get().toString(), putOption(1, json).get("value").toString());
        });
    }

    private void putOption(final ConnectFieldOptionBean updatedOption) {
        putOption(updatedOption.getId(), updatedOption);
    }

    private Either<ErrorCollection, ConnectFieldOptionBean> putOption(Integer id, final ConnectFieldOptionBean updatedOption) {
        HttpURLConnection connection = establishConnection(id.toString(), HttpMethod.PUT);
        connection = sendObject(connection, updatedOption);
        return readOutput(connection, ConnectFieldOptionBean.class);
    }

    private JsonNode putOption(Integer id, final String value) {
        String json = String.format("{ \"id\" : %d, \"value\" : %s }", id, value);
        HttpURLConnection connection = establishConnection(id.toString(), HttpMethod.PUT);
        connection = sendData(connection, json);
        return readOutputAsGenericJson(connection);
    }

    private JsonNode createOption(String json) {
        HttpURLConnection connection = establishConnection("", HttpMethod.POST);
        sendData(connection, String.format("{\"value\" : %s }", json));
        return readOutputAsGenericJson(connection);
    }

    private List<ConnectFieldOptionBean> readOptions() throws Exception {
        return readOptions(0, 1000).getValues();
    }

    private PageBean<ConnectFieldOptionBean> readOptions(Integer startAt, Integer maxResults) throws Exception {
        HttpURLConnection httpURLConnection = establishConnection(String.format("?startAt=%d&maxResults=%d", startAt, maxResults), HttpMethod.GET);
        Either<ErrorCollection, PageBean<ConnectFieldOptionBean>> allOptions = readOutput(httpURLConnection, new TypeToken<PageBean<ConnectFieldOptionBean>>() {
        }.getType());

        return allOptions.right().get();
    }

    private JsonNode readOutputAsGenericJson(final HttpURLConnection connection) {
        return readOutput(connection, str -> Json.parse(str).get()).right().get();
    }

    private <T> Either<ErrorCollection, T> readOutput(final HttpURLConnection connection, Class<T> type) {
        return readOutput(connection, str -> gson.fromJson(str, type));
    }

    private <T> Either<ErrorCollection, T> readOutput(final HttpURLConnection connection, Type type) {
        return readOutput(connection, str -> gson.fromJson(str, type));
    }

    private <T> Either<ErrorCollection, T> readOutput(final HttpURLConnection connection, Function<String, T> parser) {
        try {
            int responseCode = connection.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) {
                String result = IOUtils.toString(connection.getInputStream());
                return Either.right(parser.apply(result));
            } else {
                String result = IOUtils.toString(connection.getErrorStream());
                return Either.left(gson.fromJson(result, ErrorCollection.class));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private HttpURLConnection establishConnection(String path, HttpMethod method) {
        try {
            URI url = URI.create(restPath + path);

            String sharedSecret = checkNotNull(installHandlerServlet.getInstallPayload().getSharedSecret());
            String jwt = AddonTestUtils.generateJwtSignature(method, url, addonKey, sharedSecret, baseUrl, null);

            URL urlWithJwt = new URL(url + (path.contains("?") ? "&" : "?") + "jwt=" + jwt);

            HttpURLConnection connection = (HttpURLConnection) urlWithJwt.openConnection();
            connection.setRequestMethod(method.name());
            connection.setDoOutput(true);
            connection.setRequestProperty("content-type", "application/json");
            return connection;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private HttpURLConnection sendObject(HttpURLConnection connection, @Nullable Object data) {
        return data != null ? sendData(connection, gson.toJson(data)) : connection;
    }

    private HttpURLConnection sendData(HttpURLConnection connection, @Nullable String rawData) {
        try {
            if (rawData != null) {
                connection.getOutputStream().write(rawData.getBytes());
            }
            return connection;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
