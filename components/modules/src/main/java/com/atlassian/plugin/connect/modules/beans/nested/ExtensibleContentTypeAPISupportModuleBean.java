package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.Set;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeAPISupportModuleBeanBuilder;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class ExtensibleContentTypeAPISupportModuleBean extends BaseModuleBean
{
    private String createUrl;
    private Set<String> supportedContainerTypes;
    private Set<String> supportedContainedTypes;

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
        supportedContainerTypes = ObjectUtils.defaultIfNull(supportedContainerTypes, Sets.newHashSet());
        supportedContainedTypes = ObjectUtils.defaultIfNull(supportedContainedTypes, Sets.newHashSet());
    }

    public String getCreateUrl()
    {
        return createUrl;
    }

    public Set<String> getSupportedContainerTypes()
    {
        return supportedContainerTypes;
    }

    public Set<String> getSupportedContainedTypes()
    {
        return supportedContainedTypes;
    }
}