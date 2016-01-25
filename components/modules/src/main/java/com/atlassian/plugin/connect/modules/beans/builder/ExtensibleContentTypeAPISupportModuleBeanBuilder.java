package com.atlassian.plugin.connect.modules.beans.builder;

import java.util.Set;

import com.atlassian.plugin.connect.modules.beans.nested.ExtensibleContentTypeAPISupportModuleBean;

public class ExtensibleContentTypeAPISupportModuleBeanBuilder
        extends BaseModuleBeanBuilder<ExtensibleContentTypeAPISupportModuleBeanBuilder, ExtensibleContentTypeAPISupportModuleBean>
{
    private String createUrl;
    private Boolean isDirectlyUnderSpaceSupported;
    private Set<String> supportedContainerTypes;
    private Set<String> supportedChildrenTypes;

    public ExtensibleContentTypeAPISupportModuleBeanBuilder()
    {
    }

    public ExtensibleContentTypeAPISupportModuleBeanBuilder withCreateURL(String createUrl)
    {
        this.createUrl = createUrl;
        return this;
    }

    public ExtensibleContentTypeAPISupportModuleBeanBuilder withIsDirectlyUnderSpaceSupported(boolean isDirectlyUnderSpaceSupported)
    {
        this.isDirectlyUnderSpaceSupported = isDirectlyUnderSpaceSupported;
        return this;
    }

    public ExtensibleContentTypeAPISupportModuleBeanBuilder withSupportedContainerTypes(Set<String> supportedContainerTypes)
    {
        this.supportedContainerTypes = supportedContainerTypes;
        return this;
    }

    public ExtensibleContentTypeAPISupportModuleBeanBuilder withSupportedChildrenTypes(Set<String> supportedChildrenTypes)
    {
        this.supportedChildrenTypes = supportedChildrenTypes;
        return this;
    }

    public ExtensibleContentTypeAPISupportModuleBean build()
    {
        return new ExtensibleContentTypeAPISupportModuleBean(this);
    }
}
