package com.atlassian.plugin.connect.plugin.iframe.servlet.context;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * This is non-operating component but feel free to implement it if you need context parameters for confluence.
 */

@ConfluenceComponent
public class ConfluenceContextFactory implements ProductSpecificContextFactory {

    @Autowired
    public ConfluenceContextFactory() {}

    public Map<String, Object> createProductSpecificContext(final ModuleContextParameters params) {
        return ImmutableMap.of();
    }
}
