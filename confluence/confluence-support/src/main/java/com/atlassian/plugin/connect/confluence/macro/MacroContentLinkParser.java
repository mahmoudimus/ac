package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.plugin.connect.api.util.UriBuilderUtils;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 *
 */
@ConfluenceComponent
public class MacroContentLinkParser
{
    private static final Logger log = LoggerFactory.getLogger(MacroContentLinkParser.class);

    // this used to be implemented via a regex, but turned out to be very slow for large content
    public String parse(RemotablePluginAccessor remotablePluginAccessor, String content, Map<String, String[]> macroParameters)
    {
        if (content == null)
        {
            return content;
        }
        StringBuilder processedContent = new StringBuilder();
        int lastPos = 0;
        int pos = content.indexOf("sign://");
        while (pos > -1)
        {
            processedContent.append(content.substring(lastPos, pos));
            lastPos = pos;

            String signToken = "sign://" + remotablePluginAccessor.getBaseUrl().getAuthority();
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
                                UriBuilderUtils.addQueryParameters(b, macroParameters);

                                String urlToEmbed =
                                        remotablePluginAccessor.signGetUrl(b.toUri().toJavaUri(), ImmutableMap.<String, String[]>of());
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
