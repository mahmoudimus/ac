package com.atlassian.plugin.connect.plugin.module.confluence;

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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MacroContentLinkParserTest
{
    private static final ImmutableMap<String, String> EMPTY = ImmutableMap.of();

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

    public static final String EXPECTED_MACRO_BODY_WITH_SUBSTITUTED_URL =
            "<p>\n" +
                    "   <img src='http://Macintosh.local:3000/baseball.png' height='16' width='16'>baseball\n" +
                    "   <a href='http://blah.confluence.atlassian.com:1990/plugins/servlet/redirect/oauth?app_key=mykey&app_url=%2FviewSport%2F10%3Ffoo%3Dbar'>Edit Sport</a>\n" +
                    "</p>\n";

    @Before
    public void initParser() throws URISyntaxException
    {
        macroContentLinkParser = new MacroContentLinkParser(confluenceSettingsManager);
        when(remotablePluginAccessor.getDisplayUrl()).thenReturn(new URI("http://Macintosh.local:3000"));
        when(confluenceSettingsManager.getGlobalSettings().getBaseUrl()).thenReturn("http://blah.confluence.atlassian.com:1990");
        when(remotablePluginAccessor.getKey()).thenReturn("mykey");
    }

    @Test
    public void replacesSignUrlsWithCallToSigningService()
    {
        assertThat(macroContentLinkParser.parse(remotablePluginAccessor, MACRO_BODY_WITH_SIGN_URL, EMPTY), is(EXPECTED_MACRO_BODY_WITH_SUBSTITUTED_URL));
    }

    @Test
    public void replacesSignUrlForBaseUrl()
    {
        assertThat(macroContentLinkParser.parse(remotablePluginAccessor,
                "<a href='sign://Macintosh.local:3000'>Edit Sport</a>", EMPTY),
                is("<a href='http://blah.confluence.atlassian.com:1990/plugins/servlet/redirect/oauth?app_key=mykey&app_url=%2F'>Edit Sport</a>"));
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
        // TODO: should we throw an error instead
        String invalidUrl = "<a href='sign:???";
        assertThat(macroContentLinkParser.parse(remotablePluginAccessor, invalidUrl, EMPTY), is(invalidUrl));
    }

    @Test
    public void returnsNullWhenContentIsNull()
    {
        assertThat(macroContentLinkParser.parse(remotablePluginAccessor, null, EMPTY), is((String)null));
    }
}
