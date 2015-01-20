package com.atlassian.plugin.connect.plugin.iframe.servlet.context;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

import java.util.Map;

/**
 * This factory create context required by JIRA or Confluence.
 * Returned context could contains product specific objects.
 *
 * @since v1.1.20
 */
public interface ProductSpecificContextFactory {
    public Map<String, Object> createProductSpecificContext(final ModuleContextParameters params);
}
