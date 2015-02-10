package at.util;

import com.atlassian.plugin.connect.test.client.AtlassianConnectRestClient;

import it.util.TestUser;

public class ExternalAddonInstaller
{
    private final AtlassianConnectRestClient connectClient;

    public ExternalAddonInstaller(String baseUrl, TestUser user)
    {
        connectClient = new AtlassianConnectRestClient(
                baseUrl, user.getUsername(), user.getPassword());
    }

    public void install(String addonUrl)
    {
        try
        {
            connectClient.install(addonUrl);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void uninstall(String addonUrl)
    {
        try
        {
            connectClient.uninstall(addonUrl);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}