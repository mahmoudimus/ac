package com.atlassian.labs.remoteapps.test;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.StringReader;

import static com.atlassian.labs.remoteapps.api.XmlUtils.createSecureSaxReader;

/**
 *
 */
public class OAuthUtils
{
    private static String consumerKey;

    static
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try
        {
            String baseurl = OwnerOfTestedProduct.INSTANCE.getProductInstance().getBaseUrl();
            HttpGet get = new HttpGet(baseurl + "/plugins/servlet/oauth/consumer-info");
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(get, responseHandler);
            Document doc = createSecureSaxReader().read(new StringReader(responseBody));
            consumerKey = doc.getRootElement().element("key").getTextTrim();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Cannot retrieve oauth info");
        }
        httpclient.getConnectionManager().shutdown();
    }

    public static String getConsumerKey()
    {
        return consumerKey;
    }
}
