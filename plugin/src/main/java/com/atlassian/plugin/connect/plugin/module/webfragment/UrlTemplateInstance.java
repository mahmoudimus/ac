package com.atlassian.plugin.connect.plugin.module.webfragment;

import java.util.Map;
import java.util.Set;

/**
 * Represents an instance of a template for a Url being applied to a variable context for a particular user.
 * The URL template will have zero or more place holders for variables that are substituted from the context.
 * The user must have persmission to view any Atlassian resource (e.g. a Confluence page) corresponding to the context variables
 */
public interface UrlTemplateInstance
{
    /**
     * The template form of the url with place holders for variables
     * @return
     */
    String getUrlTemplate();

    /**
     * The Url after the variables have been substituted
     * @return
     */
    String getUrlString();

    /**
     * The variable names in the urlTemplate
     * @return
     */
    Set<String> getTemplateVariables();

    /**
     * The parameters in the context that are not template variables
     * @return
     */
    Map<String, String[]> getNonTemplateContextParameters();
}
