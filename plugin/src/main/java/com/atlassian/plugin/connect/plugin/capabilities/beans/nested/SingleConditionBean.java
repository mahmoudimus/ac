package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.VendorBeanBuilder;

import com.google.common.base.Objects;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.ParamsBean.newParamsBean;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @since version
 */
public class SingleConditionBean extends BaseCapabilityBean implements ConditionalBean
{
    private String condition;
    private Boolean invert;
    private Map<String,String> params;

    public SingleConditionBean()
    {
        this.condition = "";
        this.invert = false;
        this.params = newHashMap();
    }

    public SingleConditionBean(SingleConditionBeanBuilder builder)
    {
        super(builder);

        if(null == condition)
        {
            this.condition = "";
        }
        if(null == invert)
        {
            this.invert = false;
        }
        if(null == params)
        {
            this.params = newHashMap();
        }
    }

    public String getCondition()
    {
        return condition;
    }

    public Map<String,String> getParams()
    {
        return params;
    }

    public Boolean isInvert()
    {
        return invert;
    }

    public static SingleConditionBeanBuilder newSingleConditionBean()
    {
        return new SingleConditionBeanBuilder();
    }

    public static SingleConditionBeanBuilder newSingleConditionBean(SingleConditionBean defaultBean)
    {
        return new SingleConditionBeanBuilder(defaultBean);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(condition, invert, params);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof SingleConditionBean))
        {
            return false;
        }
        else
        {
            final SingleConditionBean that = (SingleConditionBean) obj;
            return Objects.equal(condition, that.condition) &&
                    Objects.equal(invert, that.invert) &&
                    Objects.equal(params, that.params);
        }
    }
}
