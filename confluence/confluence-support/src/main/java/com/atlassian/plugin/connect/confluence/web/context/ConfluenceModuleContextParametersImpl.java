package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.spi.web.context.HashMapModuleContextParameters;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 *
 */
public class ConfluenceModuleContextParametersImpl extends HashMapModuleContextParameters implements ConfluenceModuleContextParameters {

    public ConfluenceModuleContextParametersImpl(final Map<String, Object> originalContext) {
        super(originalContext);
    }

    @Override
    public void addPage(final AbstractPage page) {
        if (page != null) {
            put(ConfluenceModuleContextFilter.PAGE_ID, Long.toString(page.getId()));
            put(ConfluenceModuleContextFilter.PAGE_VERSION, Long.toString(page.getVersion()));
            put(ConfluenceModuleContextFilter.PAGE_TYPE, page.getType());
            addSpace(page.getSpace());
        }
    }

    @Override
    public void addSpace(final Space space) {
        if (space != null) {
            if (!StringUtils.isEmpty(space.getKey())) {
                put(ConfluenceModuleContextFilter.SPACE_KEY, space.getKey());
                put(ConfluenceModuleContextFilter.SPACE_ID, Long.toString(space.getId()));
            }
        }
    }

    @Override
    public void addContent(ContentEntityObject content) {
        if (content != null) {
            put(ConfluenceModuleContextFilter.CONTENT_ID, Long.toString(content.getId()));
            put(ConfluenceModuleContextFilter.CONTENT_VERSION, Integer.toString(content.getVersion()));
            put(ConfluenceModuleContextFilter.CONTENT_TYPE, content.getType());

            if (content instanceof CustomContentEntityObject) {
                CustomContentEntityObject customContent = (CustomContentEntityObject) content;
                put(ConfluenceModuleContextFilter.CONTENT_PLUGIN, customContent.getPluginModuleKey());
            }
        }
    }
}
