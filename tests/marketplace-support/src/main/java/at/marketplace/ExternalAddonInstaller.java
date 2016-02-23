package at.marketplace;

import at.marketplace.ConnectAddonRepresentation.Highlight;
import cc.plural.jsonij.JPath;
import cc.plural.jsonij.Value;
import cc.plural.jsonij.parser.ParserException;
import com.atlassian.plugin.connect.test.common.client.AtlassianConnectRestClient;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.base.Function;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ExternalAddonInstaller {
    public static final String ADDONS_REST_PATH = "/rest/2.0-beta/addons/";
    public static final String VENDORS_REST_PATH = "/rest/2.0-beta/vendors/";
    private static final String IMAGE_REST_PATH = "/rest/2.0-beta/assets/image/";
    public static final String ATLASSIAN_LABS_ID = "33202";

    public static final int TIMEOUT_MS = 30 * 1000;
    private static final String TEST_ADDON_VERSION = "0001";
    public static final ConnectAddonRepresentation DEFAULT_ADDON = ConnectAddonRepresentation.builder()
            .withDescriptorUrl("https://bitbucket.org/atlassianlabs/ac-acceptance-test-addon/raw/addon-" + TEST_ADDON_VERSION + "/atlassian-connect.json")
            .withLogoUrl("https://bitbucket.org/atlassianlabs/ac-acceptance-test-addon/raw/addon-" + TEST_ADDON_VERSION + "/simple-logo.png")
            .withName("Connect Test Addon v" + TEST_ADDON_VERSION) // Must be < 40 characters
            .withVendorId(ATLASSIAN_LABS_ID)
            .withHighlights(
                    new Highlight("I wanna write the very test", "<strong>That no test ever was</strong>"),
                    new Highlight("To catch bugs is your behest", "<strong>To fix them is my cause</strong>"),
                    new Highlight("I will travel the happy paths", "<strong>Searching far and wide...</strong>")
            )
            .withTagline("An Acceptance Test add-on")
            .withSummary("Regressions: gotta catch them all")
            .build();

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final AtlassianConnectRestClient connectClient;
    private final ConnectAddonRepresentation addon;
    private final URL mpacUrl;
    private final String productBaseUrl;

    private String screenshotAsset;
    private String thumbnailAsset;
    private String descriptorUrl;

    private enum ImageType {
        THUMBNAIL,
        FULL_SIZE
    }

    public ExternalAddonInstaller(String productBaseUrl, TestUser user) {
        this(productBaseUrl, user, DEFAULT_ADDON);
    }

    public ExternalAddonInstaller(String productBaseUrl, TestUser user, ConnectAddonRepresentation addon) {
        this.addon = mergeWithDefault(addon);
        this.productBaseUrl = productBaseUrl;
        connectClient = new AtlassianConnectRestClient(
                productBaseUrl, user.getUsername(), user.getPassword());
        mpacUrl = MarketplaceSettings.baseUrl();
    }

    private ConnectAddonRepresentation mergeWithDefault(ConnectAddonRepresentation addon) {
        Iterator<Highlight> highlights = defaultIfNull(
                addon.getHighlights(),
                DEFAULT_ADDON.getHighlights()
        ).iterator();

        return ConnectAddonRepresentation.builder()
                .withDescriptorUrl(defaultIfNull(addon.getDescriptorUrl(), DEFAULT_ADDON.getDescriptorUrl()))
                .withVendorId(defaultIfNull(addon.getVendorId(), DEFAULT_ADDON.getVendorId()))
                .withName(defaultIfNull(addon.getName(), DEFAULT_ADDON.getName()))
                .withSummary(defaultIfNull(addon.getSummary(), DEFAULT_ADDON.getSummary()))
                .withTagline(defaultIfNull(addon.getTagline(), DEFAULT_ADDON.getTagline()))
                .withLogoUrl(defaultIfNull(addon.getLogoUrl(), DEFAULT_ADDON.getLogoUrl()))
                .withHighlights(
                        highlights.next(),
                        highlights.next(),
                        highlights.next())
                .build();
    }

    public void install() {
        assertVendorExists();
        ensurePublicAddonExists();
        try {
            log.info("Installing add-on on instance {}", productBaseUrl);
            connectClient.install(descriptorUrl);
        } catch (Exception e) {
            throw new RuntimeException("There was error while installing our add-on on the instance.", e);
        }
    }

    public String getAddonKey() {
        return addonKey.get();
    }

    private ResettableLazyReference<String> addonKey = new ResettableLazyReference<String>() {
        @Override
        protected String create() throws Exception {
            try {
                String addonKey = transformResponse(new HttpGet(addon.getDescriptorUrl()), new HashSet<>(singletonList(200)),
                        "Error while downloading descriptor from " + addon.getDescriptorUrl(),
                        response -> {
                            try {
                                return JPath.evaluate(EntityUtils.toString(response.getEntity()), "key").getString();
                            } catch (ParserException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        }, false
                );

                if (isBlank(addonKey)) {
                    throw new IllegalStateException("Could not find key in downloaded descriptor");
                }

                return addonKey;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private void assertVendorExists() {
        try {
            if (!vendorExists(addon.getVendorId())) {
                throw new RuntimeException("The required add-on vendor (ID: " + addon.getVendorId() + ") could not be found on marketplace staging.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while checking for existence of Atlassian Labs ID", e);
        }
    }

    private void ensurePublicAddonExists() {
        try {
            if (!addonExists()) {
                screenshotAsset = createScreenshotAsset(ImageType.FULL_SIZE);
                thumbnailAsset = createScreenshotAsset(ImageType.THUMBNAIL);
                submitAddonToMarketplace();
            }

            Optional<String> descriptorUrlOption = getDescriptorUrl();
            if (!descriptorUrlOption.isPresent()) {
                descriptorUrlOption = getDescriptorUrl();
                if (!descriptorUrlOption.isPresent()) {
                    throw new IllegalStateException("Unable to retrieve descriptor url after registering add-on on marketplace");
                }
            }

            descriptorUrl = descriptorUrlOption.get();
            log.info("Using descriptor at location {}", descriptorUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String createScreenshotAsset(ImageType imageType) throws IOException {
        byte[] screenshotByteArray = toByteArray(
                getClass().getClassLoader().getResourceAsStream("marketplace/screenshot.png"));

        ByteArrayBody byteArrayBody =
                new ByteArrayBody(screenshotByteArray, ContentType.create("image/png"), "screenshot.png");

        HttpEntity body = MultipartEntityBuilder.create()
                .addPart("file", byteArrayBody)
                .build();

        HttpPost screenshotPost = new HttpPost(mpacUrl + IMAGE_REST_PATH + "screenshot" + (imageType == ImageType.THUMBNAIL ? "-thumbnail" : ""));
        screenshotPost.setEntity(body);

        log.info("Uploading a dummy " + (imageType == ImageType.THUMBNAIL ? "thumbnail " : "") + "screenshot for our test add-on");
        {
            return transformResponse(screenshotPost, new HashSet<>(singletonList(200)), "Could not create screenshots for the add-on", response ->
            {
                try {
                    String entity = EntityUtils.toString(response.getEntity());
                    return JPath.evaluate(entity, "_links/image").toString();
                } catch (IOException | ParserException e) {
                    throw new RuntimeException(e);
                }
            }, false);
        }
    }

    public void uninstall() {
        try {
            connectClient.uninstall(addonKey.get());
        } catch (Exception ignored) { /* Push on if possible */ }
    }

    private void submitAddonToMarketplace() throws IOException {
        HttpPost addonPost = new HttpPost(mpacUrl + ADDONS_REST_PATH);
        addonPost.setEntity(addonDetails());
        log.info("Registering our test add-on \"{}\" on the marketplace...", addonKey.get());
        makeRequest(addonPost, new HashSet<>(asList(200, 204)), "Could not register add-on on the marketplace");
    }

    private boolean addonExists() throws IOException {
        HttpGet get = new HttpGet(mpacUrl + ADDONS_REST_PATH + addonKey.get());
        get.addHeader(BasicScheme.authenticate(MarketplaceSettings.credentials(), "UTF-8", false));
        log.info("Checking whether the test add-on already exists on the marketplace");

        Boolean addonFound = transformResponse(
                get,
                new HashSet<>(asList(200, 404)),
                "Error checking for existence of add-on",
                response -> response.getStatusLine().getStatusCode() == 200, true);
        log.info(addonFound ? "Found an existing instance of the add-on" : "Didn't find an existing instance of the add-on");
        return addonFound;
    }

    private Optional<String> getDescriptorUrl() throws IOException {
        HttpGet get = new HttpGet(mpacUrl + ADDONS_REST_PATH + addonKey.get() + "/versions/latest");
        get.addHeader(BasicScheme.authenticate(MarketplaceSettings.credentials(), "UTF-8", false));
        log.info("Getting descriptor url for add-on {}", addonKey.get());

        return Optional.ofNullable(transformResponse(
                get,
                new HashSet<>(asList(200, 404)),
                "Error trying to retrieve descriptor href for add-on",
                response ->
                {
                    if (response.getStatusLine().getStatusCode() != 200) {
                        return null;
                    }

                    try {
                        String entity = EntityUtils.toString(response.getEntity());
                        Value hrefValue = JPath.evaluate(entity, "_embedded/artifact/_links/binary/href");
                        return (hrefValue == null) ? null : hrefValue.getString();
                    } catch (ParserException | IOException e) {
                        throw new RuntimeException("Error parsing out existing descriptor url", e);
                    }
                }, true));
    }

    private boolean vendorExists(String vendorId) throws IOException {
        HttpGet get = new HttpGet(mpacUrl + VENDORS_REST_PATH + vendorId);
        get.addHeader(BasicScheme.authenticate(MarketplaceSettings.credentials(), "UTF-8", false));
        log.info("Checking whether vendor with ID {} exists", vendorId);

        Boolean vendorFound = transformResponse(
                get,
                new HashSet<>(asList(200, 404)),
                "Error checking for existence of vendor",
                response -> response.getStatusLine().getStatusCode() == 200, true);
        return vendorFound;
    }

    private void makeRequest(HttpUriRequest request, Set<Integer> acceptableCodes, String errorMessage)
            throws IOException {
        transformResponse(request, acceptableCodes, errorMessage, null, true);
    }

    private <T> T transformResponse(HttpUriRequest request, Set<Integer> acceptableCodes,
                                    String errorMessage, Function<CloseableHttpResponse, T> transformer, boolean forceJson)
            throws IOException {
        CloseableHttpResponse response = null;
        T result = null;
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(MarketplaceSettings.credentialsProvider())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setSocketTimeout(TIMEOUT_MS)
                        .setConnectTimeout(TIMEOUT_MS)
                        .setConnectionRequestTimeout(TIMEOUT_MS)
                        .build())
                .build()) {
            if (forceJson) {
                request.addHeader("Content-Type", "application/json");
            }
            request.addHeader("Cache-Control", "no-cache");
            response = client.execute(request);
            if (!acceptableCodes.isEmpty() && !acceptableCodes.contains(response.getStatusLine().getStatusCode())) {
                throw new AcceptanceTestMarketplaceException(
                        errorMessage,
                        response);
            }
            if (transformer != null) {
                result = transformer.apply(response);
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return result;
    }

    private StringEntity addonDetails() throws IOException {
        String addonEntity = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("marketplace/addon.json"));
        addonEntity = (StringUtils.replace(addonEntity, "<%=vendor id goes here=>", addon.getVendorId()));
        addonEntity = StringUtils.replace(addonEntity, "<%=key goes here=>", addonKey.get());
        addonEntity = StringUtils.replace(addonEntity, "<%=name goes here=>", addon.getName());
        addonEntity = StringUtils.replace(addonEntity, "<%=descriptor url goes here=>", addon.getDescriptorUrl());
        addonEntity = StringUtils.replace(addonEntity, "<%=logo url goes here=>", addon.getLogoUrl());
        addonEntity = StringUtils.replace(addonEntity, "<%=screenshot asset goes here=>", screenshotAsset);
        addonEntity = StringUtils.replace(addonEntity, "<%=thumbnail asset goes here=>", thumbnailAsset);
        int highlightNumber = 0;
        for (Highlight highlight : addon.getHighlights()) {
            highlightNumber++;
            addonEntity = StringUtils.replace(addonEntity, "<%= highlight title " + highlightNumber + " goes here =>", highlight.getTitle());
            addonEntity = StringUtils.replace(addonEntity, "<%= highlight body " + highlightNumber + " goes here =>", highlight.getBody());
        }
        addonEntity = StringUtils.replace(addonEntity, "<%= summary goes here =>", addon.getSummary());
        addonEntity = StringUtils.replace(addonEntity, "<%= tagline goes here =>", addon.getTagline());
        log.info("Add-on entity: {}", addonEntity);
        return new StringEntity(addonEntity);
    }
}
