package it.common.rest;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Preconditions;

import java.net.HttpURLConnection;
import java.net.URL;

public class AddOnPropertyClient
{
    private final String baseUrl;
    private final String restPath;
    private final ConnectRunner runner;

    public AddOnPropertyClient(TestedProduct testedProduct, ConnectRunner runner)
    {
        Preconditions.checkArgument(runner.getSignedRequestHandler() != null, "ConnectRunner must be able to sign JWT requests, call `.addJWT(ConnectAppServlets.installHandlerServlet())` to fix this");
        this.runner = runner;
        this.baseUrl = testedProduct.getProductInstance().getBaseUrl();
        this.restPath = baseUrl + "/rest/atlassian-connect/1/addons/";
    }

    public int putProperty(String addOnKey, String propertyKey, String propertyValue) throws Exception
    {
        URL url = new URL(restPath + addOnKey + "/properties/" + propertyKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        runner.getSignedRequestHandler().sign(url.toURI(), "PUT", null, connection);
        connection.setDoOutput(true);
        connection.getOutputStream().write(propertyValue.getBytes());
        return connection.getResponseCode();
    }
}
