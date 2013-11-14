package com.atlassian.plugin.connect.plugin.capabilities.descriptor.url;

import org.apache.commons.lang3.builder.EqualsBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>
 * A template for URLs with one or more variables as parameter values.
 * The parameters to the URL may contain variables in the form paramName=${variableName}. For example a URL might look like
 * </p>
 * <p>
 * /acmeaddon?pid=${project.id}&issueid=${issue.id}
 * </p>
 * <p>
 * where ${project.id} and ${issue.id} are variables whose value will be substituted by corresponding variables. e.g.
 * </p>
 * <p>
 * /acmeaddon?pid=15&issueid=100
 * </p>
 * <p>
 * The form with the variables (/acmeaddon?pid=${project.id}&issueid=${issue.id}) is a template for actual
 * URL instances (/acmeaddon?pid=15&issueid=100).
 * </p>
 */
public class UrlTemplate
{
    private final String urlTemplateStr;

    /**
     * Creates a URL template from a string form of the url template
     */
    public UrlTemplate(String urlTemplateStr)
    {
        this.urlTemplateStr = checkNotNull(urlTemplateStr);
    }

// TODO: Will be implemented in ACDEV-396
//    public URL createUrl(UrlTemplateContext context)
//    {
//    }

    @Deprecated // Only exposed until ACDEV-396 implemented. After that createUrl will be used instead
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
