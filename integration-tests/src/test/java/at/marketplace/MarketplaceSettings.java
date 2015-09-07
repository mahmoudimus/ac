package at.marketplace;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import java.net.MalformedURLException;
import java.net.URL;

public class MarketplaceSettings
{
    static URL baseUrl()
    {
        try
        {
            return new URL(System.getProperty(
                "mpac.baseUrl",
                "https://marketplace.stg.internal.atlassian.com"));
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    static CredentialsProvider credentialsProvider()
    {
        String username = System.getProperty("mpac.username", "");
        String password = System.getProperty("mpac.password", "");
        int port = Integer.getInteger("mpac.port", 443);

        CredentialsProvider credentials = new BasicCredentialsProvider();
        credentials.setCredentials(
                new AuthScope(baseUrl().getHost(), port),
                new UsernamePasswordCredentials(username, password));

        return credentials;
    }

    public static Credentials credentials()
    {
        String username = System.getProperty("mpac.username", "");
        String password = System.getProperty("mpac.password", "");

        return new UsernamePasswordCredentials(username, password);
    }
}
