package com.atlassian.labs.remoteapps.plugin.module.confluence;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.labs.remoteapps.plugin.RemoteAppAccessor;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.atlassian.labs.remoteapps.plugin.module.util.redirect.RedirectServlet.*;

/**
 *
 */
public class MacroContentLinkParser
{
    private final SettingsManager confluenceSettingsManager;
    private static final Logger log = LoggerFactory.getLogger(MacroContentLinkParser.class);

    public MacroContentLinkParser(SettingsManager confluenceSettingsManager)
    {
        this.confluenceSettingsManager = confluenceSettingsManager;
    }

    // this used to be implemented via a regex, but turned out to be very slow for large content
    public String parse(RemoteAppAccessor remoteAppAccessor, String content, Map<String, String> macroParameters)
    {
        StringBuilder processedContent = new StringBuilder();
        int lastPos = 0;
        int pos = content.indexOf("sign://");
        while (pos > -1)
        {
            processedContent.append(content.substring(lastPos, pos));
            lastPos = pos;

            String signToken = "sign://" + remoteAppAccessor.getDisplayUrl().getAuthority();
            char prevChar = content.charAt(pos - 1);
            if (prevChar == '\'' || prevChar == '\"')
            {
                String attr = content.substring(pos - 6, pos - 1);
                if (" src=".equals(attr) || "href=".equals(attr))
                {
                    StringBuilder url = new StringBuilder();
                    for (int urlPos = pos + signToken.length(); urlPos < content.length(); urlPos++)
                    {
                        char urlChar = content.charAt(urlPos);
                        if (urlChar == '\'' || urlChar == '\"')
                        {
                            // found url
                            String rawRelativeUrl = StringEscapeUtils.unescapeHtml(url.toString());
                            if (StringUtils.isBlank(rawRelativeUrl))
                            {
                                rawRelativeUrl = "/";
                            }

                            Uri target;
                            try
                            {
                                target = Uri.parse(rawRelativeUrl);
                                UriBuilder b = new UriBuilder(target);
                                b.addQueryParameters(macroParameters);

                                String urlToEmbed = getOAuthRedirectUrl(
                                        confluenceSettingsManager.getGlobalSettings().getBaseUrl(),
                                        remoteAppAccessor.getKey(), b.toUri().toJavaUri());
                                processedContent.append(urlToEmbed);
                            }
                            catch (IllegalArgumentException ex)
                            {
                                log.debug("Invalid relative URL: " + rawRelativeUrl, ex);
                                processedContent.append(url.toString());
                            }
                            lastPos = urlPos;
                            break;
                        }
                        else
                        {
                            url.append(urlChar);
                        }
                    }
                }
            }
            pos = content.indexOf(signToken, lastPos + 1);
        }
        processedContent.append(content.substring(lastPos));
        return processedContent.toString();
    }
}
