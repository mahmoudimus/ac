package com.atlassian.plugin.connect.modules.beans;

import java.util.List;

public interface BeanWithConditions
{

    List<ConditionalBean> getConditions();
}
