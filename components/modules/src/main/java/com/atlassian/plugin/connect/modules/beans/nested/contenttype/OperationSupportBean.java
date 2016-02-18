package com.atlassian.plugin.connect.modules.beans.nested.contenttype;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype.OperationSupportBeanBuilder;

@SchemaDefinition("operationSupport")
public class OperationSupportBean extends BaseModuleBean
{
    public OperationSupportBean()
    {
        this(new OperationSupportBeanBuilder());
    }

    public OperationSupportBean(OperationSupportBeanBuilder builder)
    {
        super(builder);
        initialise();
    }

    private void initialise()
    {
    }
}
