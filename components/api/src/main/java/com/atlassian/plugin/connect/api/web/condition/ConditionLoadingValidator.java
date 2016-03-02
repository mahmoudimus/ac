package com.atlassian.plugin.connect.api.web.condition;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BeanWithConditions;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;

import java.util.List;

/**
 * A descriptor fragment validator that asserts that web fragment conditions can be properly loaded.
 */
public interface ConditionLoadingValidator {

    void validate(Plugin plugin, ShallowConnectAddonBean addon, ConnectModuleMeta<?> moduleMeta,
                  List<? extends BeanWithConditions> beansWithConditions)
            throws ConnectModuleValidationException;
}
