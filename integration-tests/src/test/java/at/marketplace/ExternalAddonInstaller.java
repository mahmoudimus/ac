package at.marketplace;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.plugin.connect.test.client.AtlassianConnectRestClient;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
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
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.replace;

public class ExternalAddonInstaller
{
    public static final String ADDONS_REST_PATH = "/rest/2.0-beta/addons/";
    public static final String VENDORS_REST_PATH = "/rest/2.0-beta/vendors/";
    private static final String IMAGE_REST_PATH = "/rest/2.0-beta/assets/image/";
    public static final long ATLASSIAN_LABS_ID = 33202;
    private static final String TEST_ADDON_VERSION = "0001";
    public static final int TIMEOUT_MS = 30 * 1000;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final AtlassianConnectRestClient connectClient;
    private final ConnectAddonRepresentation addon;
    private final URL mpacUrl;
    private final String productBaseUrl;

    private String screenshotAsset;
    private String thumbnailAsset;
    private String descriptorUrl;

    public ExternalAddonInstaller(String productBaseUrl, TestUser user)
    {
        this(productBaseUrl, user, ConnectAddonRepresentation.builder()
                .withDescriptorUrl("https://bitbucket.org/atlassianlabs/ac-acceptance-test-addon/raw/addon-" + TEST_ADDON_VERSION + "/atlassian-connect.json")
                .withLogo("https://bitbucket.org/atlassianlabs/ac-acceptance-test-addon/raw/addon-" + TEST_ADDON_VERSION + "/simple-logo.png")
                .withKey("com.atlassian.connect.acceptance.test.addon." + TEST_ADDON_VERSION)
                .withName("Connect Test Addon v" + TEST_ADDON_VERSION) // Must be < 40 characters
                .withVendorId(ATLASSIAN_LABS_ID).build());
    }

    public ExternalAddonInstaller(String productBaseUrl, TestUser user, ConnectAddonRepresentation addon)
    {
        this.addon = addon;
        this.productBaseUrl = productBaseUrl;
        connectClient = new AtlassianConnectRestClient(
                productBaseUrl, user.getUsername(), user.getPassword());
        mpacUrl = MarketplaceSettings.baseUrl();
    }

    public void install()
    {
        assertVendorExists();
        ensurePublicAddonExists();
        try
        {
            log.info("Installing add-on on instance {}", productBaseUrl);
            connectClient.install(descriptorUrl, false);
        }
        catch (Exception e)
        {
            throw new RuntimeException("There was error while installing our add-on on the instance.", e);
        }
    }

    private void assertVendorExists()
    {
        try
        {
            if (!vendorExists(addon.getVendorId()))
            {
                throw new RuntimeException("The required add-on vendor (ID: " + addon.getVendorId() + ") could not be found on marketplace staging.");
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error while checking for existence of Atlassian Labs ID", e);
        }
    }

    private void ensurePublicAddonExists()
    {
        try
        {
            if (!addonExists())
            {
                screenshotAsset = createScreenshotAsset(false);
                thumbnailAsset = createScreenshotAsset(true);
                submitAddonToMarketplace();
            }

            Optional<String> descriptorUrlOption = getDescriptorUrl();
            if (!descriptorUrlOption.isPresent())
            {
                descriptorUrlOption = getDescriptorUrl();
                if (!descriptorUrlOption.isPresent())
                {
                    throw new IllegalStateException("Unable to retrieve descriptor url after registering add-on on marketplace");
                }
            }

            descriptorUrl = descriptorUrlOption.get();
            log.info("Using descriptor at location {}", descriptorUrl);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String createScreenshotAsset(boolean thumbnail) throws IOException
    {
        FileBody fileBody;
        try
        {
            File file = new File(getClass().getClassLoader().getResource("marketplace/screenshot.png").toURI());
            fileBody = new FileBody(file, ContentType.create("image/png"), "screenshot.png");
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }

        HttpEntity body = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .build();

        HttpPost screenshotPost = new HttpPost(mpacUrl + IMAGE_REST_PATH + "screenshot" + (thumbnail ? "-thumbnail" : ""));
        screenshotPost.setEntity(body);

        log.info("Uploading a dummy " + (thumbnail ? "thumbnail " : "") + "screenshot for our test add-on");
        {
            return transformResponse(screenshotPost, new HashSet<>(singletonList(200)), "Could not create screenshots for the add-on", new Function<CloseableHttpResponse, String>()
            {
                @Override
                public String apply(CloseableHttpResponse response)
                {
                    try
                    {
                        String entity = EntityUtils.toString(response.getEntity());
                        return JPath.evaluate(entity, "_links/image").toString();
                    }
                    catch (IOException | ParserException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }, false);
        }
    }

    public void uninstall()
    {
        try
        {
            connectClient.uninstall(addon.getKey());
        }
        catch (Exception ignored)
        { /* Push on if possible */ }
    }

    private void submitAddonToMarketplace() throws IOException
    {
        HttpPost addonPost = new HttpPost(mpacUrl + ADDONS_REST_PATH);
        addonPost.setEntity(addonDetails());
        log.info("Registering our test add-on \"{}\" on the marketplace...", addon.getKey());
        makeRequest(addonPost, new HashSet<>(asList(200, 204)), "Could not register add-on on the marketplace");
    }

    private boolean addonExists() throws IOException
    {
        HttpGet get = new HttpGet(mpacUrl + ADDONS_REST_PATH + addon.getKey());
        get.addHeader("Authorization", BasicScheme.authenticate(MarketplaceSettings.credentials(), "UTF-8"));
        log.info("Checking whether the test add-on already exists on the marketplace");

        Boolean addonFound = transformResponse(
                get,
                new HashSet<>(asList(200, 404)),
                "Error checking for existence of add-on",
                new Function<CloseableHttpResponse, Boolean>()
                {
                    @Override
                    public Boolean apply(CloseableHttpResponse response)
                    {
                        return response.getStatusLine().getStatusCode() == 200;
                    }
                }, true);
        log.info(addonFound ? "Found an existing instance of the add-on" : "Didn't find an existing instance of the add-on");
        return addonFound;
    }

    private Optional<String> getApproveUrl() throws IOException
    {
        HttpGet get = new HttpGet(mpacUrl + ADDONS_REST_PATH + addon.getKey());
        get.addHeader("Authorization", BasicScheme.authenticate(MarketplaceSettings.credentials(), "UTF-8"));
        log.info("Checking for an approval url for add-on with key {}", addon.getKey());

        return Optional.fromNullable(transformResponse(
                get,
                new HashSet<>(asList(200, 404)),
                "Error checking for approval url",
                new Function<CloseableHttpResponse, String>()
                {
                    @Override
                    public String apply(CloseableHttpResponse response)
                    {
                        if (response.getStatusLine().getStatusCode() != 200)
                        {
                            return null;
                        }

                        try
                        {
                            String entity = EntityUtils.toString(response.getEntity());
                            Value approveValue = JPath.evaluate(entity, "_links/approve/href");
                            return approveValue == null ? null : approveValue.getString();
                        }
                        catch (ParserException | IOException e)
                        {
                            throw new RuntimeException("Error parsing approval url", e);
                        }
                    }
                }, true));
    }

    private Optional<String> getDescriptorUrl() throws IOException
    {
        HttpGet get = new HttpGet(mpacUrl + ADDONS_REST_PATH + addon.getKey() + "/versions/latest");
        get.addHeader("Authorization", BasicScheme.authenticate(MarketplaceSettings.credentials(), "UTF-8"));
        log.info("Getting descriptor url for add-on {}", addon.getKey());

        return Optional.fromNullable(transformResponse(
                get,
                new HashSet<>(asList(200, 404)),
                "Error trying to retrieve descriptor href for add-on",
                new Function<CloseableHttpResponse, String>()
                {
                    @Override
                    public String apply(CloseableHttpResponse response)
                    {
                        if (response.getStatusLine().getStatusCode() != 200)
                        {
                            return null;
                        }

                        try
                        {
                            String entity = EntityUtils.toString(response.getEntity());
                            Value hrefValue = JPath.evaluate(entity, "_embedded/artifact/_links/binary/href");
                            return (hrefValue == null) ? null : hrefValue.getString();
                        }
                        catch (ParserException | IOException e)
                        {
                            throw new RuntimeException("Error parsing out existing descriptor url", e);
                        }
                    }
                }, true));
    }

    private boolean vendorExists(String vendorId) throws IOException
    {
        HttpGet get = new HttpGet(mpacUrl + VENDORS_REST_PATH + vendorId);
        get.addHeader("Authorization", BasicScheme.authenticate(MarketplaceSettings.credentials(), "UTF-8"));
        log.info("Checking whether vendor with ID {} exists", vendorId);

        Boolean vendorFound = transformResponse(
                get,
                new HashSet<>(asList(200, 404)),
                "Error checking for existence of vendor",
                new Function<CloseableHttpResponse, Boolean>()
                {
                    @Override
                    public Boolean apply(CloseableHttpResponse response)
                    {
                        return response.getStatusLine().getStatusCode() == 200;
                    }
                }, true);
        return vendorFound;
    }

    private void makeRequest(HttpUriRequest request, Set<Integer> acceptableCodes, String errorMessage)
            throws IOException
    {
        transformResponse(request, acceptableCodes, errorMessage, null, true);
    }

    private <T> T transformResponse(HttpUriRequest request, Set<Integer> acceptableCodes,
            String errorMessage, Function<CloseableHttpResponse, T> transformer, boolean forceJson)
            throws IOException
    {
        CloseableHttpResponse response = null;
        T result = null;
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(MarketplaceSettings.credentialsProvider())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setSocketTimeout(TIMEOUT_MS)
                        .setConnectTimeout(TIMEOUT_MS)
                        .setConnectionRequestTimeout(TIMEOUT_MS)
                        .build())
                .build())
        {
            if (forceJson)
            {
                request.addHeader("Content-Type", "application/json");
            }
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
            if (response != null)
            {
                response.close();
            }
        }
        return result;
    }

    private StringEntity addonDetails() throws IOException
    {
        String addonEntity = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("marketplace/addon.json"));
        addonEntity = (replace(addonEntity, "<%=vendor id goes here=>", addon.getVendorId()));
        addonEntity = replace(addonEntity, "<%=key goes here=>", addon.getKey());
        addonEntity = replace(addonEntity, "<%=name goes here=>", addon.getName());
        addonEntity = replace(addonEntity, "<%=descriptor url goes here=>", addon.getDescriptorUrl());
        addonEntity = replace(addonEntity, "<%=logo url goes here=>", addon.getLogoUrl());
        addonEntity = replace(addonEntity, "<%=screenshot asset goes here=>", screenshotAsset);
        addonEntity = replace(addonEntity, "<%=thumbnail asset goes here=>", thumbnailAsset);
        log.info("Add-on entity: {}", addonEntity);
        return new StringEntity(addonEntity);
    }
}