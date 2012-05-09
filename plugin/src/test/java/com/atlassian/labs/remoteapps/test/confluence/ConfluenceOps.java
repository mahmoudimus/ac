package com.atlassian.labs.remoteapps.test.confluence;

import com.atlassian.pageobjects.ProductInstance;
import org.apache.commons.codec.binary.Base64;
import redstone.xmlrpc.XmlRpcArray;
import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.XmlRpcStruct;

import java.net.MalformedURLException;
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

    public Map setAnonymousPage(ProductInstance product, String spaceKey, String titlePrefix, String content) throws MalformedURLException, XmlRpcFault
    {
        long id = System.currentTimeMillis();
        XmlRpcClient client = getClientWithoutAuthorization(product);
        XmlRpcStruct struct = new XmlRpcStruct();
        struct.put("title", titlePrefix + "_" + id);
        struct.put("space", spaceKey);
        struct.put("content", content);
        XmlRpcStruct page = (XmlRpcStruct) client.invoke( "confluence2.storePage", new Object[] { "", struct } );
        return page;
    }

    public int search(ProductInstance product, String query) throws MalformedURLException, XmlRpcFault
    {
        final int maxResults = 10;
        XmlRpcClient client = getClient(product);

        XmlRpcArray searchResults = (XmlRpcArray) client.invoke("confluence2.search", new Object[] {"", query, maxResults});
        return searchResults.size();
    }

    private XmlRpcClient getClient(ProductInstance product) throws MalformedURLException
    {
        XmlRpcClient client = getClientWithoutAuthorization(product);
        client.setRequestProperty("Authorization", getAuthHeader());
        return client;
    }

    private XmlRpcClient getClientWithoutAuthorization(ProductInstance product) throws
            MalformedURLException
    {
        final String url = product.getBaseUrl() + "/rpc/xmlrpc";
        return new XmlRpcClient(url, false);
    }

    private String getAuthHeader()
    {
        byte[] authBytes = "admin:admin".getBytes(Charset.defaultCharset());
        return "Basic " + new String(Base64.encodeBase64(authBytes));
    }

}
