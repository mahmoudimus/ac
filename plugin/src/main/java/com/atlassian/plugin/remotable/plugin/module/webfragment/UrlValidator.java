package com.atlassian.plugin.remotable.plugin.module.webfragment;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collections;

/**
 * Checks if a URL is valid after variable substitution
 */
@Component
public class UrlValidator
{
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    public UrlValidator(UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }

    /**
     * Replaces all variables with empty string then validates the url.
     * @param url url to validate
     * @throws IllegalArgumentException if the url is not valid; see {@link URI#create(String)}
     */
    public void validate(String url)
    {
        URI.create(urlVariableSubstitutor.replace(url, Collections.<String, Object>emptyMap()));
    }
}
