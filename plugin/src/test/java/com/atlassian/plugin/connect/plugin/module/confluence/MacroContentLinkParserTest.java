package com.atlassian.plugin.connect.plugin.module.confluence;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MacroContentLinkParserTest
{
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SettingsManager confluenceSettingsManager;

    @Mock
    private RemotablePluginAccessor remotablePluginAccessor;

    private MacroContentLinkParser macroContentLinkParser;

    private static final String MACRO_BODY =
            "<p>\n" +
                    "   <img src='http://Macintosh.local:3000/baseball.png' height='16' width='16'>baseball\n" +
                    "   <a href='sign://Macintosh.local:3000/viewSport/10?foo=bar'>Edit Sport</a>\n" +
                    "</p>\n";

    public static final String EXPECTED_PARSED_MACRO_BODY =
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
        Map<String, String> macroParameters = ImmutableMap.of();
        String url = macroContentLinkParser.parse(remotablePluginAccessor, MACRO_BODY, macroParameters);
        assertThat(url, is(EXPECTED_PARSED_MACRO_BODY));
    }

}
