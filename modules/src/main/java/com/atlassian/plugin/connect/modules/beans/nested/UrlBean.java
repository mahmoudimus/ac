package com.atlassian.plugin.connect.modules.beans.nested;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;

/**
 * #### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#URL_EXAMPLE}
 * @schemaTitle URL
 * @since 1.0
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
