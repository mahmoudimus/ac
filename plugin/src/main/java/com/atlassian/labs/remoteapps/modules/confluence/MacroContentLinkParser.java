package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.streams.api.common.uri.Uri;
import com.atlassian.streams.api.common.uri.UriBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class MacroContentLinkParser
{
    private static final String URL_SIGNING_PATTERN = "\\s+(href|src)=['\"]sign://%s(.*?)['\"]";

    private final SettingsManager confluenceSettingsManager;

    public MacroContentLinkParser(SettingsManager confluenceSettingsManager)
    {
        this.confluenceSettingsManager = confluenceSettingsManager;
    }

    public String parse(ApplicationLink link, String content, Map<String, String> macroParameters)
    {
        final Pattern urlSigningPattern = Pattern.compile(String.format(URL_SIGNING_PATTERN, link.getDisplayUrl().getAuthority()));

        Matcher matcher = urlSigningPattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find())
        {
            String attributeName = matcher.group(1);
            String rawRelativeUrl = StringEscapeUtils.unescapeHtml(matcher.group(2));
            if (StringUtils.isBlank(rawRelativeUrl))
                rawRelativeUrl = "/";

            Uri target = Uri.parse(rawRelativeUrl);
            UriBuilder b = new UriBuilder(target);
            b.addQueryParameters(macroParameters);

            String urlToEmbed = String.format("%s/plugins/servlet/oauthRedirect?app_link_id=%s&app_url=%s", confluenceSettingsManager.getGlobalSettings().getBaseUrl(), GeneralUtil.urlEncode(link.getId().get()), GeneralUtil.urlEncode(b.toUri().toString()));

            String replacement = String.format(" %s=\"%s\"", attributeName, urlToEmbed);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
