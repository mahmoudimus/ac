package it.common.rest;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

public class AddOnPropertyClient
{
    private final String baseUrl;
    private final String restPath;
    private final ConnectRunner runner;

    private enum Method
    {
        PUT, DELETE
    }

    public AddOnPropertyClient(TestedProduct testedProduct, ConnectRunner runner)
    {
        Preconditions.checkArgument(runner.getSignedRequestHandler() != null, "ConnectRunner must be able to sign JWT requests, call `.addJWT(ConnectAppServlets.installHandlerServlet())` to fix this");
        this.runner = runner;
        this.baseUrl = testedProduct.getProductInstance().getBaseUrl();
        this.restPath = baseUrl + "/rest/atlassian-connect/1/addons/";
    }

    public int putProperty(String addOnKey, String propertyKey, String propertyValue) throws Exception
    {
        HttpURLConnection connection = prepareConnection(addOnKey, propertyKey, Method.PUT);
        connection.setDoOutput(true);
        connection.getOutputStream().write(propertyValue.getBytes());
        return connection.getResponseCode();
    }

    public int deleteProperty(String addOnKey, String propertyKey, String propertyValue) throws Exception
    {
        return prepareConnection(addOnKey, propertyKey, Method.DELETE).getResponseCode();
    }

    private HttpURLConnection prepareConnection(final String addOnKey, final String propertyKey, Method method) throws IOException, URISyntaxException
    {
        URL url = new URL(restPath + addOnKey + "/properties/" + propertyKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method.name());
        runner.getSignedRequestHandler().sign(url.toURI(), method.name(), null, connection);
        return connection;
    }
}
