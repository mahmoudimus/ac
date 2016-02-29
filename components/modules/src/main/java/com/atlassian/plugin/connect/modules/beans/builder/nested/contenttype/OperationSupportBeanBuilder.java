package com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype;

import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.OperationSupportBean;

public class OperationSupportBeanBuilder
        extends BaseModuleBeanBuilder<OperationSupportBeanBuilder, OperationSupportBean> {
    public OperationSupportBean build() {
        return new OperationSupportBean(this);
    }

}
