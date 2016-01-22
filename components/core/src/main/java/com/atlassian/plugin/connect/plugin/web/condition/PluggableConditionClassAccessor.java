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
import java.util.function.Predicate;
import java.util.stream.Stream;

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
        return getConditionClass(new Function<ConnectConditionClassResolver.Entry, Optional<Class<? extends Condition>>>()
        {

            @Override
            public Optional<Class<? extends Condition>> apply(ConnectConditionClassResolver.Entry resolverEntry)
            {
                return resolverEntry.getConditionClassForHostContext(conditionBean);
            }
        });
    }

    @Override
    public Optional<Class<? extends Condition>> getConditionClassForInline(SingleConditionBean conditionBean)
    {
        return getConditionClass(new Function<ConnectConditionClassResolver.Entry, Optional<Class<? extends Condition>>>()
        {

            @Override
            public Optional<Class<? extends Condition>> apply(ConnectConditionClassResolver.Entry resolverEntry)
            {
                return resolverEntry.getConditionClassForInline(conditionBean);
            }
        });
    }

    @Override
    public Optional<Class<? extends Condition>> getConditionClassForNoContext(SingleConditionBean conditionBean)
    {
        return getConditionClass(new Function<ConnectConditionClassResolver.Entry, Optional<Class<? extends Condition>>>()
        {

            @Override
            public Optional<Class<? extends Condition>> apply(ConnectConditionClassResolver.Entry resolverEntry)
            {
                return resolverEntry.getConditionClassForNoContext(conditionBean);
            }
        });
    }

    private Optional<Class<? extends Condition>> getConditionClass(
            Function<ConnectConditionClassResolver.Entry, Optional<Class<? extends Condition>>> mapper)
    {
        List<ConnectConditionClassResolver> resolvers = pluginAccessor.getEnabledModulesByClass(
                ConnectConditionClassResolver.class);
        return resolvers.stream()
                .flatMap(new Function<ConnectConditionClassResolver, Stream<ConnectConditionClassResolver.Entry>>()
                {
                    @Override
                    public Stream<ConnectConditionClassResolver.Entry> apply(ConnectConditionClassResolver resolver)
                    {
                        return resolver.getEntries().stream();
                    }
                }).map(mapper).filter(new Predicate<Optional<Class<? extends Condition>>>()
                {
                    @Override
                    public boolean test(Optional<Class<? extends Condition>> optionalConditionClass)
                    {
                        return optionalConditionClass.isPresent();
                    }
                }).map(new Function<Optional<Class<? extends Condition>>, Class<? extends Condition>>()
                {
                    @Override
                    public Class<? extends Condition> apply(Optional<Class<? extends Condition>> optionalConditionClass)
                    {
                        return optionalConditionClass.get();
                    }
                }).findFirst();
    }
}
