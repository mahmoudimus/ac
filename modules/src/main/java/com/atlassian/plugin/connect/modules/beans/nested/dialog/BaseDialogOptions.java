package com.atlassian.plugin.connect.modules.beans.nested.dialog;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class BaseDialogOptions extends BaseModuleBean
{
    /**
     *  Sets how wide the dialog is in pixels
     */
    private String width;

    public BaseDialogOptions(String width)
    {
        this.width = width;
    }

    public BaseDialogOptions()
    {

    }

    public BaseDialogOptions(BaseModuleBeanBuilder dialogOptionsBuilder)
    {
        super(dialogOptionsBuilder);
    }

    public String getWidth()
    {
        return width;
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof BaseDialogOptions))
        {
            return false;
        }

        BaseDialogOptions other = (BaseDialogOptions) otherObj;

        return new EqualsBuilder()
                .append(width, other.width)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(173, 199)
                .append(width)
                .build();
    }

}
