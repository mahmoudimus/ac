package com.atlassian.plugin.remotable.plugin.module.confluence;

import com.atlassian.confluence.setup.settings.Settings;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.remotable.spi.RemotablePluginAccessor;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public final class TestMacroContentLinkParser
{
    private static URI CONFLUENCE_BASE_URL;
    private static URI APP_BASE_URL;

    @Mock
    private RemotablePluginAccessor remotablePluginAccessor;
    @Mock
    private Settings confluenceSettings;
    @Mock
    private SettingsManager confluenceSettingsManager;

    private MacroContentLinkParser parser;
    private String appTypeId;

    static
    {
        try
        {
            APP_BASE_URL = new URI("https://potatoes-online.com");
            CONFLUENCE_BASE_URL = new URI("https://mycompany.jira.com/wiki");
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("If you see this error, grab your shotgun; the zombie apocalypse is coming.", e);
        }
    }

    private enum QuoteStyle
    {
        DOUBLE_QUOTES('"'),
        SINGLE_QUOTES('\'');

        private final char quoteChar;

        private QuoteStyle(char quoteChar)
        {
            this.quoteChar = quoteChar;
        }

        public String getQuote()
        {
            return String.valueOf(quoteChar);
        }
    }

    @Before
    public void setup() throws URISyntaxException
    {
        MockitoAnnotations.initMocks(this);
        parser = new MacroContentLinkParser(confluenceSettingsManager);

        when(confluenceSettingsManager.getGlobalSettings()).thenReturn(confluenceSettings);
        when(confluenceSettings.getBaseUrl()).thenReturn(CONFLUENCE_BASE_URL.toString());

        appTypeId = "foo";
        when(remotablePluginAccessor.getKey()).thenReturn(appTypeId);
        when(remotablePluginAccessor.getDisplayUrl()).thenReturn(APP_BASE_URL);
    }

    /**
     * Basic functionality test.
     */
    @Test
    public void testReplaceRemotablePluginsUrl() throws Exception
    {
        String testRelativeUrl = "/purchase";
        String testText = "Purchase Now!";
        String rawContent = makeSignableLink(testRelativeUrl, testText);

        runTest(rawContent, null, testRelativeUrl, testText);
    }

    /**
     * Tests that query parameters in the original URL are preserved when replacing it with the redirecting URL.
     */
    @Test
    public void testReplaceUrlWithQueryParams() throws Exception
    {
        String testRelativeUrl = "/purchase?potatoes=256&potato_id=blarg";
        String testText = "Buy Some <b>Potatoes</b>";
        String rawContent = makeSignableLink(testRelativeUrl, testText);

        runTest(rawContent, null, testRelativeUrl, testText);
    }

    /**
     * Tests that the parser correctly detects href attributes using single quotes instead of double quotes.
     */
    @Test
    public void testReplaceUrlAlternativeQuoteForm() throws Exception
    {
        String testRelativeUrl = "/redeem-voucher";
        String testText = "Redeem Your Potato Points!";
        String rawContent = makeSignableLink(testRelativeUrl, testText, QuoteStyle.SINGLE_QUOTES);

        runTest(rawContent, null, testRelativeUrl, testText, QuoteStyle.SINGLE_QUOTES);
    }

    /**
     * Tests that the parser ignores URLs that do not link to the Remotable Plugin
     */
    @Test
    public void testPlainTextUrlIsNotReplaced() throws Exception
    {
        String testUrl = "http://www.google.com.au";
        String testText = "Don't click on me";
        String rawContent = makeAnchor(testUrl, testText);

        runTestExpectingNoReplacement(rawContent, null);
    }

    /**
     * Tests that the parser ignores URLs to the Remotable Plugin that have not opted-in to be signed.
     */
    @Test
    public void testOptOut()
    {
        String rawContent = makeAnchor(APP_BASE_URL.toString() + "/welcome", "You don't need to be logged in to view this page.");

        runTestExpectingNoReplacement(rawContent, null);
    }

    /**
     * Tests that the parser correctly replaces URLs within an image src attribute.
     */
    @Test
    public void testAppUrlInImageSourceIsReplaced() throws Exception
    {
        String testRelativeUrl = "/french-fries.png";
        String rawContent = makeSignableImage(testRelativeUrl);

        runTestExpectingImageReplacement(rawContent, null, testRelativeUrl);
    }

    /**
     * Tests that the parser ignores URLs to the Remotable Plugin that do not appear within href or src attributes.
     */
    @Test
    public void testUrlInPlainTextIsNotReplaced() throws Exception
    {
        String rawContent = "Hello, here is some <strong>basic</strong> HTML with some " + makeSignableUrl("/kumara.html") + "plain-text URLs";
        runTestExpectingNoReplacement(rawContent, null);
    }

    /**
     * Tests that the parser includes parameters from the parent macro in the redirecting URL.
     */
    @Test
    public void testReplaceWithMacroParameters() throws Exception
    {
        String testRelativeUrl = "/vegetables/sweet-potato?raw=true";
        String testText = "Browse Catalogue";

        String rawContent = makeSignableLink(testRelativeUrl, testText);
        Map<String, String> testParameters = Maps.newHashMap();
        testParameters.put("page_id", "124563");
        testParameters.put("vegetables_on", "true");
        testParameters.put("space_key", "ds");

        runTest(rawContent, testParameters, testRelativeUrl, testText);
    }

    /**
     * Tests that the parser correctly redirects URLs to the Remotable Plugin that have no path component.
     */
    @Test
    public void testReplaceRootUrl() throws Exception
    {
        String testRelativeUrl = "";
        String testText = "Potatoes Online";

        String rawContent = makeSignableLink(testRelativeUrl, testText);

        runTest(rawContent, null, "/", testText);
    }

    private String makeSignableUrl(String remotePluginRelativeUrl)
    {
        return String.format("sign://%s%s", APP_BASE_URL.getAuthority(), remotePluginRelativeUrl);
    }

    private String makeImage(String imageSource)
    {
        return String.format("<img src=\"%s\">", imageSource);
    }

    private String makeSignableImage(String relativeImageSource)
    {
        return makeImage(makeSignableUrl(relativeImageSource));
    }

    private String makeAnchor(String anchorHref, String anchorContent, QuoteStyle quotes)
    {
        return String.format("<a href=%s%s%s>%s</a>", quotes.getQuote(), anchorHref, quotes.getQuote(), anchorContent);
    }

    private String makeAnchor(String anchorHref, String anchorContent)
    {
        return makeAnchor(anchorHref, anchorContent, QuoteStyle.DOUBLE_QUOTES);
    }

    private String makeSignableLink(String relativeUrl, String anchorContent, QuoteStyle quotes)
    {
        return makeAnchor(makeSignableUrl(relativeUrl), anchorContent, quotes);
    }

    private String makeSignableLink(String relativeUrl, String anchorContent)
    {
        return makeSignableLink(relativeUrl, anchorContent, QuoteStyle.DOUBLE_QUOTES);
    }

    private String makeRedirectUrl(String appLinkId, String remotePluginRelativeUrl, @NotNull Map<String, String> extraParameters) throws UnsupportedEncodingException
    {
        StringBuilder builder = new StringBuilder(remotePluginRelativeUrl);
        boolean hasParams = remotePluginRelativeUrl.contains("?");

        for (String key : extraParameters.keySet())
        {
            String formatPattern = hasParams ? "&%s=%s"
                    : "?%s=%s";
            builder.append(String.format(formatPattern, key, extraParameters.get(key)));
        }
        String appUrl = builder.toString();
        return String.format("%s/plugins/servlet/redirect/oauth?app_key=%s&app_url=%s", CONFLUENCE_BASE_URL.toString(), URLEncoder.encode(appLinkId, "utf-8"), URLEncoder.encode(appUrl, "utf-8"));
    }

    private void runTestExpectingImageReplacement(String rawContent, @Nullable Map<String, String> macroParameters, String expectedRemotablePluginRelativeImageSource) throws Exception
    {
        if (macroParameters == null)
            macroParameters = Maps.newHashMap();

        String actualContent = parser.parse(remotablePluginAccessor, rawContent, macroParameters);
        String expectedContent = makeImage(makeRedirectUrl(appTypeId, expectedRemotablePluginRelativeImageSource, macroParameters));

        assertEquals(expectedContent, actualContent);
    }

    private void runTest(String rawContent, @Nullable Map<String, String> macroParameters, String expectedRemotablePluginRelativeUrl, String expectedAnchorText) throws Exception
    {
        runTest(rawContent, macroParameters, expectedRemotablePluginRelativeUrl, expectedAnchorText,
                QuoteStyle.DOUBLE_QUOTES);
    }

    private void runTest(String rawContent, @Nullable Map<String, String> macroParameters, String expectedRemotablePluginRelativeUrl, String expectedAnchorText, QuoteStyle quoteStyle) throws Exception
    {
        if (macroParameters == null)
            macroParameters = Maps.newHashMap();

        String actualContent = parser.parse(remotablePluginAccessor, rawContent, macroParameters);
        String expectedContent = makeAnchor(makeRedirectUrl(appTypeId, expectedRemotablePluginRelativeUrl, macroParameters), expectedAnchorText, quoteStyle);

        assertEquals(expectedContent, actualContent);
    }

    private void runTestExpectingNoReplacement(String rawContent, @Nullable Map<String, String> macroParameters)
    {
        if (macroParameters == null)
            macroParameters = Maps.newHashMap();

        String actualContent = parser.parse(remotablePluginAccessor, rawContent, macroParameters);

        assertEquals(rawContent, actualContent); // actualContent should not be modified from the original.
    }
}
