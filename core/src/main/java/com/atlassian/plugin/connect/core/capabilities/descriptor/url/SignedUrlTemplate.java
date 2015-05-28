package com.atlassian.plugin.connect.core.capabilities.descriptor.url;

/**
 * An implementation of URLTemplate that signs the URL's after variable substitution.
 * Prior to Signing it validates the users access to any resources identified in the url
 * TODO: implement in ACDEV-498
 */
public class SignedUrlTemplate extends UrlTemplate
{
    public SignedUrlTemplate(String urlTemplateStr)
    {
        super(urlTemplateStr);
    }
}
