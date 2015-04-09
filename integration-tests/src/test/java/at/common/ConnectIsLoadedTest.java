package at.common;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Category(OnDemandAcceptanceTest.class)
public class ConnectIsLoadedTest
{
    // these constants could be sourced from something that the integration tests provides,
    // if we try to run the integration tests in our clouds acceptance tests
    private final static String USERNAME = "admin";
    private final static String PASSWORD = "admin";
    private final static String CONNECT_PLUGIN_KEY = "com.atlassian.plugins.atlassian-connect-plugin";
    private final static String KEY_PROPERTY = "key";

    @Test
    public void connectShouldBeLoadedInJira() throws IOException
    {
        connectShouldBeLoaded(new LocalTestEnvironmentData().getBaseUrl().toString());
    }

    @Test
    public void connectShouldBeLoadedInConfluence() throws IOException
    {
        TestedProduct<?> confluenceProduct = TestedProductFactory.create(FixedConfluenceTestedProduct.class);
        connectShouldBeLoaded(confluenceProduct.getProductInstance().getBaseUrl());
    }

    private void connectShouldBeLoaded(String productBaseUrl) throws IOException
    {
        URI url = URI.create(productBaseUrl + "/rest/plugins/1.0/"); // NB: needs the trailing slash otherwise it 404s (!!)
        HttpURLConnection connection = (HttpURLConnection) url.toURL().openConnection();
        String auth = USERNAME + ":" + PASSWORD;
        connection.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64(auth.getBytes())));
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode());

        String responseText = IOUtils.toString(connection.getInputStream());
        Collection<JsonObject> acPlugins = findAcPlugins(new JsonParser().parse(responseText).getAsJsonObject().get("plugins").getAsJsonArray());

        final String reason = String.format("Expecting exactly 1 plugin to have key '%s' but found %d. Connect appears to not be installed. Plugins listing: %s",
                CONNECT_PLUGIN_KEY, acPlugins.size(), responseText);
        assertThat(reason, acPlugins.size(), is(1));
    }

    private Collection<JsonObject> findAcPlugins(JsonArray plugins)
    {
        Collection<JsonObject> acPlugins = new ArrayList<JsonObject>(1);

        for (int i = 0 ; i < plugins.size() ; ++i)
        {
            JsonObject plugin = plugins.get(i).getAsJsonObject();
            final JsonElement key = plugin.get(KEY_PROPERTY);

            if (null != key && CONNECT_PLUGIN_KEY.equals(key.getAsString()))
            {
                acPlugins.add(plugin);
            }
        }

        return acPlugins;
    }
}
