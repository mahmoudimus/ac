package com.atlassian.plugin.remotable.test.pageobjects.confluence;

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
    private final String baseUrl;

    public ConfluenceOps(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public Map setPage(String spaceKey, String titlePrefix, String content) throws MalformedURLException, XmlRpcFault
    {
        long id = System.currentTimeMillis();
        XmlRpcClient client = getClient();
        XmlRpcStruct struct = new XmlRpcStruct();
        struct.put("title", titlePrefix + "_" + id);
        struct.put("space", spaceKey);
        struct.put("content", content);
        XmlRpcStruct page = (XmlRpcStruct) client.invoke( "confluence2.storePage", new Object[] { "", struct } );
        return page;
    }

    public Map addComment(String pageId, String content) throws MalformedURLException, XmlRpcFault
    {
        XmlRpcClient client = getClient();
        XmlRpcStruct struct = new XmlRpcStruct();
        struct.put("pageId", pageId);
        struct.put("content", content);
        XmlRpcStruct page = (XmlRpcStruct) client.invoke( "confluence2.addComment", new Object[] { "", struct } );
        return page;
    }

    public Map setAnonymousPage(String spaceKey, String titlePrefix, String content) throws MalformedURLException, XmlRpcFault
    {
        long id = System.currentTimeMillis();
        XmlRpcClient client = getClientWithoutAuthorization();
        XmlRpcStruct struct = new XmlRpcStruct();
        struct.put("title", titlePrefix + "_" + id);
        struct.put("space", spaceKey);
        struct.put("content", content);
        XmlRpcStruct page = (XmlRpcStruct) client.invoke( "confluence2.storePage", new Object[] { "", struct } );
        return page;
    }

    public int search(String query) throws MalformedURLException, XmlRpcFault
    {
        final int maxResults = 10;
        XmlRpcClient client = getClient();

        XmlRpcArray searchResults = (XmlRpcArray) client.invoke("confluence2.search", new Object[] {"", query, maxResults});
        return searchResults.size();
    }

    private XmlRpcClient getClient() throws MalformedURLException
    {
        XmlRpcClient client = getClientWithoutAuthorization();
        client.setRequestProperty("Authorization", getAuthHeader());
        return client;
    }

    private XmlRpcClient getClientWithoutAuthorization() throws
            MalformedURLException
    {
        final String url = baseUrl + "/rpc/xmlrpc";
        return new XmlRpcClient(url, false);
    }

    private String getAuthHeader()
    {
        byte[] authBytes = "admin:admin".getBytes(Charset.defaultCharset());
        return "Basic " + new String(Base64.encodeBase64(authBytes));
    }

}
