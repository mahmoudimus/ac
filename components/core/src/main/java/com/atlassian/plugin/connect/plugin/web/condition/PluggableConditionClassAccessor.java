package com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.web.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
@ExportAsService
public class PluggableConditionClassAccessor implements ConditionClassAccessor
{

    private PluginAccessor pluginAccessor;

    @Autowired
    public PluggableConditionClassAccessor(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public Optional<Class<? extends Condition>> getConditionClassForHostContext(SingleConditionBean conditionBean)
    {
        return getConditionClass(resolverEntry -> resolverEntry.getConditionClassForHostContext(conditionBean));
    }

    @Override
    public Optional<Class<? extends Condition>> getConditionClassForInline(SingleConditionBean conditionBean)
    {
        return getConditionClass(resolverEntry -> resolverEntry.getConditionClassForInline(conditionBean));
    }

    @Override
    public Optional<Class<? extends Condition>> getConditionClassForNoContext(SingleConditionBean conditionBean)
    {
        return getConditionClass(resolverEntry -> resolverEntry.getConditionClassForNoContext(conditionBean));
    }

    private Optional<Class<? extends Condition>> getConditionClass(
            Function<ConnectConditionClassResolver.Entry, Optional<Class<? extends Condition>>> mapper)
    {
        List<ConnectConditionClassResolver> resolvers = pluginAccessor.getEnabledModulesByClass(ConnectConditionClassResolver.class);
        return resolvers
            .stream()
            .flatMap(resolver -> resolver.getEntries().stream())
            .map(mapper)
            .filter(Optional::isPresent).map(Optional::get)
            .findFirst();
    }
}
