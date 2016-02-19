package com.atlassian.plugin.connect.modules.beans.nested.contenttype;

import java.util.Set;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype.APISupportBeanBuilder;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.ObjectUtils;

@SchemaDefinition("apiSupport")
public class APISupportBean extends BaseModuleBean
{
    @CommonSchemaAttributes(defaultValue = "storage")
    private BodyType bodyType;

    @Required
    private Set<String> supportedContainerTypes;

    private Set<String> supportedContainedTypes;

    @StringSchemaAttributes(format = "uri")
    @CommonSchemaAttributes(defaultValue = "")
    private String onCreateUrl;

    @StringSchemaAttributes(format = "uri")
    @CommonSchemaAttributes(defaultValue = "")
    private String onUpdateUrl;

    @StringSchemaAttributes(format = "uri")
    @CommonSchemaAttributes(defaultValue = "")
    private String onDeleteUrl;

    public APISupportBean()
    {
        this(new APISupportBeanBuilder());
    }

    public APISupportBean(APISupportBeanBuilder builder)
    {
        super(builder);
        initialise();
    }

    private void initialise()
    {
        supportedContainerTypes = ObjectUtils.defaultIfNull(supportedContainerTypes, Sets.newHashSet());
        supportedContainedTypes = ObjectUtils.defaultIfNull(supportedContainedTypes, Sets.newHashSet());
    }

    public BodyType getBodyType()
    {
        return bodyType;
    }

    public Set<String> getSupportedContainerTypes()
    {
        return supportedContainerTypes;
    }

    public Set<String> getSupportedContainedTypes()
    {
        return supportedContainedTypes;
    }

    public String getOnCreateUrl()
    {
        return onCreateUrl;
    }

    public String getOnUpdateUrl()
    {
        return onUpdateUrl;
    }

    public String getOnDeleteUrl()
    {
        return onDeleteUrl;
    }
}