package it.servlet;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class InstallHandlerServlet extends HttpServlet
{
    /**
     * Example payload:
     *
     * key: 'my-add-on',
     * clientKey: 'Confluence:8179004655',
     * publicKey: 'MIGfMA0GCSqGS...IDAQAB',
     * sharedSecret: '6c7e1ffd-387b-47c8-8b4b-6c510d3cdfb5',
     * serverVersion: '5615',
     * pluginsVersion: '1.1.0.SNAPSHOT',
     * baseUrl: 'http://pstreule:1990/confluence',
     * productType: 'confluence',
     * description: 'host.consumer.default.description',
     * eventType: 'installed
     */
    public static class InstallPayload
    {
        private String key;
        private String clientKey;
        private String publicKey;
        private String sharedSecret;
        private String serverVersion;
        private String pluginsVersion;
        private String baseUrl;
        private String productType;
        private String description;
        private String eventType;

        public String getKey()
        {
            return key;
        }

        public String getClientKey()
        {
            return clientKey;
        }

        public String getPublicKey()
        {
            return publicKey;
        }

        public String getSharedSecret()
        {
            return sharedSecret;
        }

        public String getServerVersion()
        {
            return serverVersion;
        }

        public String getPluginsVersion()
        {
            return pluginsVersion;
        }

        public String getBaseUrl()
        {
            return baseUrl;
        }

        public String getProductType()
        {
            return productType;
        }

        public String getDescription()
        {
            return description;
        }

        public String getEventType()
        {
            return eventType;
        }
    }

    private InstallPayload installPayload;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        installPayload = new Gson().fromJson(req.getReader(), InstallPayload.class);
    }

    public InstallPayload getInstallPayload()
    {
        return installPayload;
    }
}
