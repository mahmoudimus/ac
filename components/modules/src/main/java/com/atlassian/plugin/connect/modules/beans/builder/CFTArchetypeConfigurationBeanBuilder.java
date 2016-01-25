package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.CustomFieldArchetype;
import com.atlassian.plugin.connect.modules.beans.CFTArchetypeConfiguration;

public class CFTArchetypeConfigurationBeanBuilder
        extends BaseModuleBeanBuilder<CFTArchetypeConfigurationBeanBuilder, CFTArchetypeConfiguration>
{
    private CustomFieldArchetype archetype;

    public CFTArchetypeConfigurationBeanBuilder(CustomFieldArchetype archetype)
    {
        this.archetype = archetype;
    }

    public CFTArchetypeConfigurationBeanBuilder(CFTArchetypeConfiguration archetypeConfiguration)
    {
        super(archetypeConfiguration);
        this.archetype = archetypeConfiguration.getArchetype();
    }

    public CFTArchetypeConfigurationBeanBuilder withArchetype(CustomFieldArchetype type)
    {
        this.archetype = type;
        return this;
    }

    @Override
    public CFTArchetypeConfiguration build()
    {
        return new CFTArchetypeConfiguration(this);
    }

}
