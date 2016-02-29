package com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype;

import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.APISupportBean;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.BodyType;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.IndexingBean;

import java.util.Set;

/**
 * Defines API support for Extensible Content Type
 *
 * @since 1.1.77
 */
public class APISupportBeanBuilder
        extends BaseModuleBeanBuilder<APISupportBeanBuilder, APISupportBean> {
    private BodyType bodyType;

    private Set<String> supportedContainerTypes;

    private Set<String> supportedContainedTypes;

    private String onCreateUrl;

    private String onUpdateUrl;

    private String onDeleteUrl;

    /**
     * Defines how would the content type be indexed
     */
    private IndexingBean indexing;

    public APISupportBeanBuilder() {
    }

    public APISupportBeanBuilder withBodyType(String bodyType) {
        this.bodyType = BodyType.valueOf(bodyType.toUpperCase());
        return this;
    }

    public APISupportBeanBuilder withSupportedContainerTypes(Set<String> supportedContainerTypes) {
        this.supportedContainerTypes = supportedContainerTypes;
        return this;
    }

    public APISupportBeanBuilder withSupportedContainedTypes(Set<String> supportedChildrenTypes) {
        this.supportedContainedTypes = supportedChildrenTypes;
        return this;
    }

    public APISupportBeanBuilder withOnCreateUrl(String onCreateUrl) {
        this.onCreateUrl = onCreateUrl;
        return this;
    }

    public APISupportBeanBuilder withOnUpdateUrl(String onUpdateUrl) {
        this.onUpdateUrl = onUpdateUrl;
        return this;
    }

    public APISupportBeanBuilder withOnDeleteUrl(String onDeleteUrl) {
        this.onDeleteUrl = onDeleteUrl;
        return this;
    }

    public APISupportBeanBuilder withIndexing(IndexingBean indexing) {
        this.indexing = indexing;
        return this;
    }

    public APISupportBean build() {
        return new APISupportBean(this);
    }
}
