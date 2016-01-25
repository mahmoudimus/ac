package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.CFTArchetypeConfigurationBeanBuilder;

public class CFTArchetypeConfiguration extends BaseModuleBean
{
    @Required
    private CustomFieldArchetype archetype;

    public CFTArchetypeConfiguration()
    {
        this.archetype = CustomFieldArchetype.TEXT;
    }

    public CFTArchetypeConfiguration(CFTArchetypeConfigurationBeanBuilder builder)
    {
        super(builder);
    }

    public CustomFieldArchetype getArchetype()
    {
        return archetype;
    }

    public void setType(final CustomFieldArchetype archetype)
    {
        this.archetype = archetype;
    }
}
