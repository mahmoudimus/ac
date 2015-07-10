package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MacroContentLinkParserTest
{
    private static final ImmutableMap<String, String[]> EMPTY = ImmutableMap.of();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SettingsManager confluenceSettingsManager;

    @Mock
    private RemotablePluginAccessor remotablePluginAccessor;

    private MacroContentLinkParser macroContentLinkParser;

    private static final String MACRO_BODY_WITH_SIGN_URL =
            "<p>\n" +
                    "   <img src='http://Macintosh.local:3000/baseball.png' height='16' width='16'>baseball\n" +
                    "   <a href='sign://Macintosh.local:3000/viewSport/10?foo=bar'>Edit Sport</a>\n" +
                    "</p>\n";

    private static final String SIGNED_URL = "look at me I'm a signed URL";

    private static final String EXPECTED_MACRO_BODY_WITH_SUBSTITUTED_URL =
            "<p>\n" +
                    "   <img src='http://Macintosh.local:3000/baseball.png' height='16' width='16'>baseball\n" +
                    "   <a href='" + SIGNED_URL + "'>Edit Sport</a>\n" +
                    "</p>\n";

    @Before
    public void initParser() throws URISyntaxException
    {
        macroContentLinkParser = new MacroContentLinkParser();
        when(remotablePluginAccessor.getBaseUrl()).thenReturn(new URI("http://Macintosh.local:3000"));
        when(confluenceSettingsManager.getGlobalSettings().getBaseUrl()).thenReturn("http://blah.confluence.atlassian.com:1990");
        when(remotablePluginAccessor.getKey()).thenReturn("mykey");
    }

    @Test
    public void replacesSignUrlsWithSignedUrlToAddon() throws URISyntaxException
    {
        assertThat(parseMacroAndSignUrl(MACRO_BODY_WITH_SIGN_URL, EMPTY), is(EXPECTED_MACRO_BODY_WITH_SUBSTITUTED_URL));
    }

    @Test
    public void callsUrlSignerWithCorrectUrl() throws URISyntaxException
    {
        parseMacroAndSignUrl(MACRO_BODY_WITH_SIGN_URL, EMPTY);
        verify(remotablePluginAccessor).signGetUrl(new URI("/viewSport/10?foo=bar"), EMPTY);
    }

    @Test
    public void callsUrlSignerWithEmptyParams() throws URISyntaxException
    {
        // TODO: I think this is correct cause the macro params are added as query params
        parseMacroAndSignUrl(MACRO_BODY_WITH_SIGN_URL, ImmutableMap.of("key1", new String[]{"value1"}));
        verify(remotablePluginAccessor).signGetUrl(any(URI.class), eq(EMPTY));
    }

    @Test
    public void appendsMacroParametersAsQueryParams() throws URISyntaxException
    {
        parseMacroAndSignUrl("<a href='sign://Macintosh.local:3000/viewSport/10?foo=bar'>Edit Sport</a>",
                ImmutableMap.of("key1", new String[]{"value1"}, "key2", new String[]{"value2"}));
        verify(remotablePluginAccessor).signGetUrl(new URI("/viewSport/10?foo=bar&key1=value1&key2=value2"), EMPTY);
    }

    @Test
    public void replacesSignUrlForBaseUrl() throws URISyntaxException
    {
        assertThat(parseMacroAndSignUrl("<a href='sign://Macintosh.local:3000'>Edit Sport</a>", EMPTY),
                is("<a href='" + SIGNED_URL + "'>Edit Sport</a>"));
    }

    @Test
    public void replacesSignUrlForUrlWithSrc() throws URISyntaxException
    {
        assertThat(parseMacroAndSignUrl("<img src='sign://Macintosh.local:3000'>Edit Sport</a>", EMPTY),
                is("<img src='" + SIGNED_URL + "'>Edit Sport</a>"));
    }


    @Ignore // Current code clips it instead of either throwing error or ignoring it. Dangerous and flakey. Fix
    @Test
    public void ignoresSignUrlsWithWrongAuthority()
    {
        // TODO: should we throw an error instead
        String macroWithWrongAuthority = "<a href='sign://Windows.local:3000/viewSport/10?foo=bar'>Edit Sport</a>";
        assertThat(macroContentLinkParser.parse(remotablePluginAccessor, macroWithWrongAuthority, EMPTY), is(macroWithWrongAuthority));
    }

    @Test
    public void ignoresSignUrlsNotInHrefOrSrcTag()
    {
        // TODO: not sure why this is important. What's the implication of substituting them anywhere?
        String signUrlNotInHref = "<a ref='sign://Macintosh.local:3000/viewSport/10?foo=bar'>Edit Sport</a>";
        assertThat(macroContentLinkParser.parse(remotablePluginAccessor, signUrlNotInHref, EMPTY), is(signUrlNotInHref));
    }

    @Test
    public void ignoresSignUrlsWhenUrlIsInvalid()
    {
        String invalidUrl = "<a href='sign:???";
        assertThat(macroContentLinkParser.parse(remotablePluginAccessor, invalidUrl, EMPTY), is(invalidUrl));
    }

    @Test
    public void returnsNullWhenContentIsNull()
    {
        assertThat(macroContentLinkParser.parse(remotablePluginAccessor, null, EMPTY), is((String) null));
    }

    private String parseMacroAndSignUrl(String macroBody, Map<String, String[]> macroParams)
    {
        when(remotablePluginAccessor.signGetUrl(any(URI.class), any(Map.class))).thenReturn(SIGNED_URL);
        return macroContentLinkParser.parse(remotablePluginAccessor, macroBody, macroParams);
    }


}
