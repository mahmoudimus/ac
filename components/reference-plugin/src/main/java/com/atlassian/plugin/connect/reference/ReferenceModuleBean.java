package com.atlassian.plugin.connect.reference;

import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ReferenceModuleBean extends RequiredKeyBean
{

    private int referenceField;

    public ReferenceModuleBean(int referenceField)
    {
        this.referenceField = referenceField;
    }

    public int getReferenceField()
    {
        return referenceField;
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof ReferenceModuleBean && super.equals(otherObj)))
        {
            return false;
        }

        ReferenceModuleBean other = (ReferenceModuleBean) otherObj;

        return new EqualsBuilder()
                .append(referenceField, other.referenceField)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(12, 34)
                .append(super.hashCode())
                .append(referenceField)
                .build();
    }
}
