package com.atlassian.labs.remoteapps.test.confluence;

import com.atlassian.pageobjects.ProductInstance;
import org.apache.commons.codec.binary.Base64;
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

    private XmlRpcClient getClient(ProductInstance product) throws MalformedURLException
    {
        final String url = product.getBaseUrl() + "/rpc/xmlrpc";
        XmlRpcClient client = new XmlRpcClient(url, false);
        client.setRequestProperty("Authorization", getAuthHeader());
        return client;
    }

    private String getAuthHeader()
    {
        byte[] authBytes = "admin:admin".getBytes(Charset.defaultCharset());
        return "Basic " + new String(Base64.encodeBase64(authBytes));
    }

}
