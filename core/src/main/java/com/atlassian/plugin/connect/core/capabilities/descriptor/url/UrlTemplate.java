package com.atlassian.plugin.connect.core.capabilities.descriptor.url;

import org.apache.commons.lang3.builder.EqualsBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A template for URLs with one or more variables as parameter values.
 *
 * The parameters to the URL may contain variables in the form paramName={variableName}.
 * For example a URL might look like
 *
 * {@code /acmeaddon?pid={project.id}&issueid={issue.id}}
 *
 * where project.id and issue.id are variables whose value will be substituted by corresponding variables. e.g.
 *
 * {@code /acmeaddon?pid=15&issueid=100}
 */
public class UrlTemplate
{
    private final String urlTemplateStr;

    /**
     * Creates a URL template from a string form of the url template
     *
     * @param urlTemplateStr the string form of a URL template
     */
    public UrlTemplate(String urlTemplateStr)
    {
        this.urlTemplateStr = checkNotNull(urlTemplateStr);
    }

    @Deprecated // Only exposed until ACDEV-498 implemented. After that createUrl will be used instead
    public String getTemplateString()
    {
        return urlTemplateStr;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (obj.getClass() != getClass())
        {
            return false;
        }
        UrlTemplate rhs = (UrlTemplate) obj;
        return new EqualsBuilder()
                .append(urlTemplateStr, rhs.urlTemplateStr)
                .isEquals();
    }

}
