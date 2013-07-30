package it.confluence;

import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.remotable.test.server.RunnerSignedRequestHandler;
import it.TestConstants;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcStruct;

import java.net.MalformedURLException;
import java.net.URI;

import static com.atlassian.plugin.remotable.test.Utils.createSignedRequestHandler;
import static org.junit.Assert.assertEquals;

public final class TestConfluenceScopes
{
    private static String baseUrl;
    private static AtlassianConnectAddOnRunner addOnRunner;
    private static RunnerSignedRequestHandler signedRequestHandler;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        final String appKey = RandomStringUtils.randomAlphanumeric(20);
        baseUrl = OwnerOfTestedProduct.INSTANCE.getProductInstance().getBaseUrl();
        signedRequestHandler = createSignedRequestHandler(appKey);
        addOnRunner = new AtlassianConnectAddOnRunner(baseUrl, appKey)
                .addOAuth(signedRequestHandler)
                .addPermission(Permissions.CREATE_OAUTH_LINK)
                .addPermission("read_content")
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (addOnRunner != null)
        {
            addOnRunner.stop();
        }
    }

    @Test
    public void testCallScopedXmlRpc() throws Exception
    {
        final XmlRpcStruct space = (XmlRpcStruct) getClient(TestConstants.BETTY).invoke("confluence2.getSpace", new Object[]{"", "DS"});
        assertEquals("ds", space.getString("key"));
        assertEquals("Demonstration Space", space.getString("name"));
    }

    private XmlRpcClient getClient(String username) throws MalformedURLException
    {
        final URI url = URI.create(baseUrl + "/rpc/xmlrpc");
        XmlRpcClient client = new XmlRpcClient(url.toString() + "?user_id=" + username, false);
        client.setRequestProperty("Authorization", signedRequestHandler.getAuthorizationHeaderValue(url, "POST", username));
        return client;
    }
}
