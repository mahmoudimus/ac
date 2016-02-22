package com.atlassian.plugin.connect.test.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;

import com.google.common.base.Preconditions;

public class AddonPropertyClient {
    private final String baseUrl;
    private final String restPath;
    private final ConnectRunner runner;

    private enum Method {
        PUT, DELETE
    }

    public AddonPropertyClient(TestedProduct testedProduct, ConnectRunner runner) {
        Preconditions.checkArgument(runner.getSignedRequestHandler() != null, "ConnectRunner must be able to sign JWT requests, call `.addJWT(ConnectAppServlets.installHandlerServlet())` to fix this");
        this.runner = runner;
        this.baseUrl = testedProduct.getProductInstance().getBaseUrl();
        this.restPath = baseUrl + "/rest/atlassian-connect/1/addons/";
    }

    public int putProperty(String addonKey, String propertyKey, String propertyValue) throws Exception {
        HttpURLConnection connection = prepareConnection(addonKey, propertyKey, Method.PUT);
        connection.setDoOutput(true);
        connection.getOutputStream().write(propertyValue.getBytes());
        return connection.getResponseCode();
    }

    public int deleteProperty(String addonKey, String propertyKey, String propertyValue) throws Exception {
        return prepareConnection(addonKey, propertyKey, Method.DELETE).getResponseCode();
    }

    private HttpURLConnection prepareConnection(final String addonKey, final String propertyKey, Method method) throws IOException, URISyntaxException {
        URL url = new URL(restPath + addonKey + "/properties/" + propertyKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method.name());
        runner.getSignedRequestHandler().sign(url.toURI(), method.name(), null, connection);
        return connection;
    }
}
