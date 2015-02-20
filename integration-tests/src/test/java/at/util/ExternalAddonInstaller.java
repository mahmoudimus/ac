package at.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.plugin.connect.test.client.AtlassianConnectRestClient;

import com.google.common.base.Function;

import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import cc.plural.jsonij.JPath;
import cc.plural.jsonij.JSON;
import cc.plural.jsonij.parser.ParserException;
import it.util.TestUser;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.substringAfter;

public class ExternalAddonInstaller
{
    public static final String ADDON_KEY = "com.atlassian.connect.acceptance.test";
    public static final String ADDONS_REST_PATH = "/rest/2.0-beta/addons/";
    public static final String VENDORS_REST_PATH = "/rest/2.0-beta/vendors/";
    public static final int RANDOM_VENDOR_SUFFIX_LENGTH = 20;
    public static final String TOKENS_REST_PATH = "/rest/1.0/plugins/" + ADDON_KEY + "/tokens";

    private final AtlassianConnectRestClient connectClient;
    private String vendorId;
    private String descriptorUrl;

    public ExternalAddonInstaller(String baseUrl, TestUser user)
    {
        connectClient = new AtlassianConnectRestClient(
                baseUrl, user.getUsername(), user.getPassword());
    }

    public void install()
    {
        try
        {
            deleteAddonFromMarketplace();
            createVendor();
            registerAddonOnMarketplace();
            connectClient.install(descriptorUrl);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void uninstall()
    {
        try
        {
            connectClient.uninstall(ADDON_KEY);
            deleteAddonFromMarketplace();
            deleteVendor();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void registerAddonOnMarketplace()
            throws IOException, ParserException
    {
        HttpPost addonPost = new HttpPost(MarketplaceSettings.baseUrl() + ADDONS_REST_PATH);
        addonPost.setEntity(addonDetails());
        makeRequest(addonPost, new HashSet<Integer>(asList(200, 204)), "Could not register add-on on the marketplace");

        HttpPost tokenPost = new HttpPost(MarketplaceSettings.baseUrl() + TOKENS_REST_PATH);
        descriptorUrl = transformResponse(tokenPost, new HashSet<Integer>(asList(200)),
                "Could not get a private token for the add-on",
                new Function<CloseableHttpResponse, String>()
                {
                    @Override
                    public String apply(CloseableHttpResponse response)
                    {
                        try
                        {
                            JSON json = JSON.parse(EntityUtils.toString(response.getEntity()));
                            return JPath.evaluate(json, "versions[0]/links[0]/href").getString();
                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private void deleteAddonFromMarketplace() throws IOException
    {
        if (noAddonWithKeyFound())
        {
            return;
        }

        HttpDelete delete = new HttpDelete(MarketplaceSettings.baseUrl() + ADDONS_REST_PATH + ADDON_KEY);
        makeRequest(delete, new HashSet<Integer>(asList(200, 204, 404, 410)), "Could not delete add-on from the marketplace");
    }

    private boolean noAddonWithKeyFound() throws IOException
    {
        HttpGet get = new HttpGet(MarketplaceSettings.baseUrl() + ADDONS_REST_PATH + ADDON_KEY);
        get.addHeader("Authorization", BasicScheme.authenticate(MarketplaceSettings.credentials(), "UTF-8"));

        return transformResponse(
                get,
                new HashSet<Integer>(asList(200, 404)),
                "Error checking for existence of add-on",
                new Function<CloseableHttpResponse, Boolean>()
                {
                    @Override
                    public Boolean apply(CloseableHttpResponse response)
                    {
                        return response.getStatusLine().getStatusCode() == 404;
                    }
                });
    }

    private void createVendor() throws IOException
    {
        HttpPost post = new HttpPost(MarketplaceSettings.baseUrl() + VENDORS_REST_PATH);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(vendorDetails());

        vendorId = transformResponse(
                post,
                new HashSet<Integer>(asList(200, 204)),
                "Could not create a vendor on the marketplace",
                new Function<CloseableHttpResponse, String>()
                {
                    @Override
                    public String apply(CloseableHttpResponse response)
                    {
                        return substringAfter(response.getFirstHeader("Location").getValue(), VENDORS_REST_PATH);
                    }
                });
    }

    private void deleteVendor() throws IOException
    {
        HttpDelete delete = new HttpDelete(MarketplaceSettings.baseUrl() + VENDORS_REST_PATH + vendorId);
        makeRequest(delete, new HashSet<Integer>(asList(200, 204, 404, 410)), "Unable to delete vendor from the marketplace");
    }

    private void makeRequest(HttpUriRequest request, Set<Integer> acceptableCodes, String errorMessage)
            throws IOException
    {
        CloseableHttpResponse response = null;
        try
        {
            response = transformResponse(request, acceptableCodes, errorMessage, null);
        }
        finally
        {
            if (response != null)
            {
                response.close();
            }
        }
    }

    private <T> T transformResponse(HttpUriRequest request, Set<Integer> acceptableCodes,
            String errorMessage, Function<CloseableHttpResponse, T> transformer)
            throws IOException
    {
        CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(MarketplaceSettings.credentialsProvider())
                .build();

        CloseableHttpResponse response = null;
        T result = null;
        try
        {
            request.addHeader("content-type", "application/json");
            response = client.execute(request);
            if (!acceptableCodes.contains(response.getStatusLine().getStatusCode()))
            {
                throw new AcceptanceTestMarketplaceException(
                        errorMessage + ": ",
                        response);
            }
            if (transformer != null)
            {
                result = transformer.apply(response);
            }
        }
        finally
        {
            client.close();
            if (response != null)
            {
                response.close();
            }
        }
        return result;
    }

    private StringEntity addonDetails() throws IOException
    {
        String addonEntityTemplate = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("marketplace/addon.json"));
        return new StringEntity(replace(addonEntityTemplate, "<%=vendor id goes here=>", vendorId));
    }

    private StringEntity vendorDetails() throws IOException
    {
        String vendorTemplate = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("marketplace/vendor.json"));
        String vendorName = "Acceptance test-generated vendor " + RandomStringUtils.random(RANDOM_VENDOR_SUFFIX_LENGTH, true, false);
        return new StringEntity(replace(vendorTemplate, "<%=vendor name goes here=>", vendorName));
    }
}