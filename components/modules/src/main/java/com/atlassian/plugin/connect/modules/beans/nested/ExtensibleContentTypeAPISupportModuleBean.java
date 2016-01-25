package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.Set;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeAPISupportModuleBeanBuilder;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class ExtensibleContentTypeAPISupportModuleBean extends BaseModuleBean
{
    private String createUrl;
    private Boolean isDirectlyUnderSpaceSupported;
    private Set<String> supportedContainerTypes;
    private Set<String> supportedChildrenTypes;

    public ExtensibleContentTypeAPISupportModuleBean()
    {
        super(new ExtensibleContentTypeAPISupportModuleBeanBuilder());
        initialise();
    }

    public ExtensibleContentTypeAPISupportModuleBean(ExtensibleContentTypeAPISupportModuleBeanBuilder builder)
    {
        super(builder);
        initialise();
    }

    private void initialise()
    {
        createUrl = StringUtils.defaultString(createUrl);
        isDirectlyUnderSpaceSupported = BooleanUtils.toBooleanDefaultIfNull(isDirectlyUnderSpaceSupported, false);
        supportedContainerTypes = ObjectUtils.defaultIfNull(supportedContainerTypes, Sets.newHashSet());
        supportedChildrenTypes = ObjectUtils.defaultIfNull(supportedChildrenTypes, Sets.newHashSet());
    }

    public String getCreateUrl()
    {
        return createUrl;
    }

    public boolean getIsDirectlyUnderSpaceSupported()
    {
        return isDirectlyUnderSpaceSupported;
    }

    public Set<String> getSupportedContainerTypes()
    {
        return supportedContainerTypes;
    }

    public Set<String> getSupportedChildrenTypes()
    {
        return supportedChildrenTypes;
    }
}
