package it.confluence.contenttype;

import com.atlassian.confluence.api.model.JsonString;
import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentRepresentation;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.content.JsonContentProperty;
import com.atlassian.elasticsearch.shaded.google.common.collect.Sets;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype.APISupportBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype.OperationSupportBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype.UISupportBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.IndexingBean;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import it.confluence.ConfluenceWebDriverTestBase;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.After;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

import static it.confluence.ConfluenceWebDriverTestBase.TestSpace.DEMO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public abstract class AbstractExtensibleContentTypeTest extends ConfluenceWebDriverTestBase {
    protected final String CONTAINER_TITLE = "Test Extensible Type Container";
    protected final String TYPE_KEY_1 = "test-extensible-type-1";
    protected final String TYPE_NAME_1 = "Test Extensible Type 1";
    protected final String TYPE_KEY_2 = "test-extensible-type-2";
    protected final String TYPE_NAME_2 = "Test Extensible Type 2";

    protected static ConnectRunner remotePlugin;
    protected InstallHandlerServlet installHandlerServlet;
    protected ContentType contentType1;
    protected ContentType contentType2;
    protected String addonKey;

    public ExtensibleContentTypeModuleBean createSimpleBean(String typeKey, String typeName) {
        return createBeanWithRestriction(typeKey, typeName, Sets.newHashSet("global"), Sets.newHashSet());
    }

    public ExtensibleContentTypeModuleBean createBeanWithRestriction(String typeKey, String typeName, Set<String> restrictedContainer, Set<String> restrictedContained) {
        return new ExtensibleContentTypeModuleBeanBuilder()
                .withKey(typeKey)
                .withName(new I18nProperty(typeName, ""))
                .withUISupport(new UISupportBeanBuilder()
                        .build())
                .withOperationSupport(new OperationSupportBeanBuilder()
                        .build())
                .withAPISupport(new APISupportBeanBuilder()
                        .withSupportedContainerTypes(restrictedContainer)
                        .withSupportedContainedTypes(restrictedContained)
                        .withIndexing(new IndexingBean(false, ""))
                        .build())
                .build();
    }

    public ExtensibleContentTypeModuleBean createBeanWithContentPropertyIndexingSupport(String typeKey, String typeName, boolean indexingEnabled, String contentPropertyKey) {
        return new ExtensibleContentTypeModuleBeanBuilder()
                .withKey(typeKey)
                .withName(new I18nProperty(typeName, ""))
                .withUISupport(new UISupportBeanBuilder()
                        .build())
                .withOperationSupport(new OperationSupportBeanBuilder()
                        .build())
                .withAPISupport(new APISupportBeanBuilder()
                        .withSupportedContainerTypes(Sets.newHashSet("global"))
                        .withSupportedContainedTypes(Sets.newHashSet())
                        .withIndexing(new IndexingBean(indexingEnabled, contentPropertyKey))
                        .build())
                .build();
    }

    public void startConnectAddon(ModuleBean... beans) throws Exception {
        addonKey = AddonTestUtils.randomAddonKey();
        contentType1 = ContentType.valueOf(getCompleteContentTypeKey(TYPE_KEY_1));
        contentType2 = ContentType.valueOf(getCompleteContentTypeKey(TYPE_KEY_2));
        installHandlerServlet = new InstallHandlerServlet();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), addonKey)
                .addJWT(installHandlerServlet)
                .setAuthenticationToNone()
                .addScopes(ScopeName.READ, ScopeName.WRITE)
                .addModules("extensibleContentTypes", beans)
                .start();
    }

    @After
    public void stopConnectAddon() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
            remotePlugin = null;
        }
    }

    public void checkExtensibleContentType(String typeKey, String typeName) throws Exception {
        assertThat(String.format("Can not find extensible content type (typeKey: %s, name: %s)", typeKey, typeName),
                hasExtensibleContentType(typeKey, typeName), is(true));
    }

    public Content createContainerContent(ContentType contentType) {
        return createContent(buildContent(contentType, null, CONTAINER_TITLE));
    }

    public Content createContent(Content content) {
        return restClient.content().create(content).claim();
    }

    public JsonContentProperty createContentProperty(Content content, String key, String value) {
        JsonContentProperty contentProperty = createJsonContentProperty(content, key, value);
        return restClient.contentProperties().create(contentProperty).claim();
    }

    public JsonContentProperty updateContentProperty(JsonContentProperty contentProperty, String value) {
        JsonContentProperty updatedContentProperty = JsonContentProperty.builder(contentProperty).value(new JsonString(value)).build();
        return restClient.contentProperties().update(updatedContentProperty).claim();
    }

    private JsonContentProperty createJsonContentProperty(Content content, String key, String value) {
        return JsonContentProperty
                .builder()
                .content(content)
                .key(key)
                .value(new JsonString(value))
                .build();
    }

    public Content buildContent(ContentType contentType, Content container, String title) {
        Content.ContentBuilder content = Content
                .builder(contentType)
                .space(DEMO.getKey())
                .title(title + " " + System.currentTimeMillis());

        if (container != null) {
            content.container(container);
        }

        return content.build();
    }

    public Content buildContent(ContentType contentType, Content container, String title, String body) {
        Content.ContentBuilder content = Content
                .builder(contentType)
                .space(DEMO.getKey())
                .title(title + " " + System.currentTimeMillis())
                .body(body, ContentRepresentation.STORAGE);

        if (container != null) {
            content.container(container);
        }

        return content.build();
    }

    private boolean hasExtensibleContentType(String typeKey, String typeName) throws Exception {
        String completeTypeKey = getCompleteContentTypeKey(typeKey);
        String json = getResponse("GET", "/rest/cql/contenttypes");
        JSONArray contentTypes = (JSONArray) JSONValue.parse(json);

        for (Object contentTypeObject : contentTypes) {
            JSONObject contentType = (JSONObject) contentTypeObject;
            String type = (String) contentType.get("type");
            String label = (String) contentType.get("label");

            if (completeTypeKey.equals(type) && typeName.equals(label)) {
                return true;
            }
        }

        return false;
    }

    private String getResponse(String method, String uri) throws Exception {
        URL url = new URL(product.getProductInstance().getBaseUrl() + uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);

        return IOUtils.toString(connection.getInputStream());
    }

    public String getCompleteContentTypeKey(String typeKey) {
        return addonKey + ":" + typeKey;
    }
}
