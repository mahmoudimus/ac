package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;

/**
 * @exampleJson example: {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#URL_EXAMPLE}
 */
public class UrlBean
{
    @StringSchemaAttributes(format = "uri")
    @Required
    private String url;

    public UrlBean(String url)
    {
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    public boolean hasUrl()
    {
        return null != url;
    }

    public URI createUri() throws URISyntaxException
    {
        return null == url ? null : new URI(url);
    }

}
