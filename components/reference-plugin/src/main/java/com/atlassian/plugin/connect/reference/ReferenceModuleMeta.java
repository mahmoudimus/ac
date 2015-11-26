package com.atlassian.plugin.connect.reference;

import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;

public class ReferenceModuleMeta extends ConnectModuleMeta<ReferenceModuleBean>
{

    public ReferenceModuleMeta()
    {
        super("referenceModules", ReferenceModuleBean.class);
    }
}
