package com.atlassian.plugin.connect.core.module.webfragment;

import java.net.URI;
import java.util.Collections;

import com.atlassian.plugin.connect.api.module.webfragment.UrlVariableSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Checks if a URL is valid after variable substitution
 */
@Component
public class UrlValidator
{
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    @Autowired
    public UrlValidator(UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }

    /**
     * Replaces all variables with empty string then validates the url.
     * @param url url to validate
     * @throws IllegalArgumentException if the url is not valid; see {@link java.net.URI#create(String)}
     */
    public void validate(String url)
    {
        URI.create(urlVariableSubstitutor.replace(url, Collections.<String, Object>emptyMap()));
    }
}
