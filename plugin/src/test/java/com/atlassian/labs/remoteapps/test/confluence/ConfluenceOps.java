package com.atlassian.labs.remoteapps.test.confluence;

import com.atlassian.pageobjects.ProductInstance;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.taskdefs.condition.Http;
import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.XmlRpcStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

/**
 *
 */
public class ConfluenceOps
{
    public Map setPage(ProductInstance product, String spaceKey, String titlePrefix, String content) throws MalformedURLException, XmlRpcFault
    {
        long id = System.currentTimeMillis();
        XmlRpcClient client = getClient(product);
        XmlRpcStruct struct = new XmlRpcStruct();
        struct.put("title", titlePrefix + "_" + id);
        struct.put("space", spaceKey);
        struct.put("content", content);
        XmlRpcStruct page = (XmlRpcStruct) client.invoke( "confluence2.storePage", new Object[] { "", struct } );
        return page;
    }

    private XmlRpcClient getClient(ProductInstance product) throws MalformedURLException
    {
        final String url = product.getBaseUrl() + "/rpc/xmlrpc";
        XmlRpcClient client = new XmlRpcClient(url, false);
        client.setRequestProperty("Authorization", getAuthHeader());
        return client;
    }

    public void resetMacrosOnPage(ProductInstance productInstance, String pageId) throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection) new URL(productInstance.getBaseUrl() + "/rest/remoteapps/latest/macro/page/" + pageId).openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", getAuthHeader());

        conn.getInputStream().close();
    }

    private String getAuthHeader()
    {
        byte[] authBytes = "admin:admin".getBytes(Charset.defaultCharset());
        return "Basic " + new String(Base64.encodeBase64(authBytes));
    }

    public void resetMacrosForPlugin(ProductInstance productInstance, String pluginKey) throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection) new URL(productInstance.getBaseUrl() + "/rest/remoteapps/latest/macro/app/" + pluginKey).openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", getAuthHeader());
        conn.getInputStream().close();
    }
}
