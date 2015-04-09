package at.util;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.plugin.connect.test.client.AtlassianConnectRestClient;

import com.google.common.base.Function;

import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.plural.jsonij.JPath;
import cc.plural.jsonij.Value;
import cc.plural.jsonij.parser.ParserException;
import it.util.TestUser;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.substringAfter;

public class ExternalAddonInstaller
{
    public static final String ADDON_KEY = "com.atlassian.connect.acceptance.test";
    public static final String ADDONS_REST_PATH = "/rest/2.0-beta/addons/";
    public static final String VENDORS_REST_PATH = "/rest/2.0-beta/vendors/";
    public static final int RANDOM_VENDOR_SUFFIX_LENGTH = 20;
    public static final String TOKENS_REST_PATH = ADDONS_REST_PATH + ADDON_KEY + "/tokens/";
    public static final int MARKETPLACE_MAX_TOKENS = 50;
    public static final int TIMEOUT_MS = 30 * 1000;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final AtlassianConnectRestClient connectClient;
    private String vendorId;
    private String descriptorUrl;
    private final URL mpacUrl;

    public ExternalAddonInstaller(String baseUrl, TestUser user)
    {
        connectClient = new AtlassianConnectRestClient(
                baseUrl, user.getUsername(), user.getPassword());
        mpacUrl = MarketplaceSettings.baseUrl();
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
        catch (Exception ignored)
        { /* Push on if possible */ }
    }

    private void registerAddonOnMarketplace() throws IOException
    {
        HttpPost addonPost = new HttpPost(mpacUrl + ADDONS_REST_PATH);
        addonPost.setEntity(addonDetails());
        log.info("Registering our test add-on \"{}\" on the marketplace...", ADDON_KEY);
        makeRequest(addonPost, new HashSet<Integer>(asList(200, 204)), "Could not register add-on on the marketplace");

        HttpPost tokenPost = new HttpPost(mpacUrl + TOKENS_REST_PATH);
        descriptorUrl = transformResponse(tokenPost, new HashSet<Integer>(asList(200)),
                "Could not get a private token for the add-on",
                new Function<CloseableHttpResponse, String>()
                {
                    @Override
                    public String apply(CloseableHttpResponse response)
                    {
                        try
                        {
                            String entity = EntityUtils.toString(response.getEntity());
                            return JPath.evaluate(entity, "_links/descriptor/href").getString();
                        }
                        catch (ParserException e)
                        {
                            throw new RuntimeException(e);
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private void deleteAddonFromMarketplace() throws IOException
    {
        if (!addonExists())
        {
            return;
        }

        deleteTokens();
        HttpDelete addonDelete = new HttpDelete(mpacUrl + ADDONS_REST_PATH + ADDON_KEY);
        log.info("Deleting our test add-on \"{}\"...", ADDON_KEY);
        makeRequest(addonDelete, new HashSet<Integer>(), "Could not delete add-on from the marketplace");
    }

    private void deleteTokens() throws IOException
    {
        for (String token : getTokens())
        {
            log.info("Deleting token " + token + " ...");
            HttpDelete delete = new HttpDelete(mpacUrl + TOKENS_REST_PATH + token);

            // 404 is fine; it means the token's actually gone and we just hit a stale cache
            makeRequest(delete, new HashSet<Integer>(asList(204, 404)), "Could not delete token");
        }
    }

    private Collection<String> getTokens() throws IOException
    {
        HttpGet get = new HttpGet(mpacUrl + TOKENS_REST_PATH);
        get.addHeader("Authorization", BasicScheme.authenticate(MarketplaceSettings.credentials(), "UTF-8"));

        log.info("Getting the tokens...");

        // 404 is fine; it means the add-on is actually gone and we just hit a stale cache
        return transformResponse(get, new HashSet<Integer>(asList(200, 404)), "Could not get add-on tokens",
                new Function<CloseableHttpResponse, Collection<String>>()
                {
                    @Override
                    public Collection<String> apply(CloseableHttpResponse response)
                    {
                        final Set<String> tokens = new HashSet<String>(MARKETPLACE_MAX_TOKENS);
                        try
                        {
                            if (response.getStatusLine().getStatusCode() == 404)
                            {
                                return emptySet();
                            }
                            String entity = EntityUtils.toString(response.getEntity());
                            Value tokensArray = JPath.evaluate(entity, "_embedded/tokens");
                            int tokensCount = tokensArray.size();

                            for (int i = 0; i < tokensCount; i++)
                            {
                                tokens.add(JPath.evaluate(tokensArray.get(i), "token").getString());
                            }
                            return tokens;
                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private boolean addonExists() throws IOException
    {
        HttpGet get = new HttpGet(mpacUrl + ADDONS_REST_PATH + ADDON_KEY);
        get.addHeader("Authorization", BasicScheme.authenticate(MarketplaceSettings.credentials(), "UTF-8"));
        log.info("Checking whether the test add-on already exists on the marketplace");

        Boolean addonFound = transformResponse(
                get,
                new HashSet<Integer>(asList(200, 404)),
                "Error checking for existence of add-on",
                new Function<CloseableHttpResponse, Boolean>()
                {
                    @Override
                    public Boolean apply(CloseableHttpResponse response)
                    {
                        return response.getStatusLine().getStatusCode() == 200;
                    }
                });
        log.info(addonFound ? "Found an existing instance of the add-on" : "Didn't find an existing instance of the add-on");
        return addonFound;
    }

    private void createVendor() throws IOException
    {
        HttpPost post = new HttpPost(mpacUrl + VENDORS_REST_PATH);
        post.setHeader("Content-Type", "application/json");
        log.info("Creating a vendor on the marketplace...");
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
        log.info("Cleaning up the created vendor...");
        HttpDelete delete = new HttpDelete(mpacUrl + VENDORS_REST_PATH + vendorId);
        makeRequest(delete, new HashSet<Integer>(asList(200, 204, 404, 410)), "Unable to delete vendor from the marketplace");
    }

    private void makeRequest(HttpUriRequest request, Set<Integer> acceptableCodes, String errorMessage)
            throws IOException
    {
        transformResponse(request, acceptableCodes, errorMessage, null);
    }

    private <T> T transformResponse(HttpUriRequest request, Set<Integer> acceptableCodes,
            String errorMessage, Function<CloseableHttpResponse, T> transformer)
            throws IOException
    {
        CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(MarketplaceSettings.credentialsProvider())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setSocketTimeout(TIMEOUT_MS)
                        .setConnectTimeout(TIMEOUT_MS)
                        .setConnectionRequestTimeout(TIMEOUT_MS)
                        .build())
                .build();

        CloseableHttpResponse response = null;
        T result = null;
        try
        {
            request.addHeader("content-type", "application/json");
            request.addHeader("Cache-Control", "no-cache");
            response = client.execute(request);
            if (!acceptableCodes.isEmpty() && !acceptableCodes.contains(response.getStatusLine().getStatusCode()))
            {
                throw new AcceptanceTestMarketplaceException(
                        errorMessage,
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
        log.info("Vendor name: \"{}\"", vendorName);
        return new StringEntity(replace(vendorTemplate, "<%=vendor name goes here=>", vendorName));
    }
}