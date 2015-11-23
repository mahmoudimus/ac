package com.atlassian.plugin.connect.spi.condition;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.dom4j.Element;

import java.util.List;

import static com.google.common.collect.Iterables.transform;

public class ConnectConditionModuleDescriptor extends AbstractModuleDescriptor<ConnectConditionModuleDescriptor.ConnectConditionClassProvider> {

    private Iterable<ConnectConditionDefinition> conditionClasses;

    public ConnectConditionModuleDescriptor(ModuleFactory moduleFactory) {
        super(moduleFactory);
    }

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException {
        super.init(plugin, element);
        this.conditionClasses = loadClassNames(element);
    }

    private Iterable<ConnectConditionDefinition> loadClassNames(Element element) {
        Iterable<Element> elements = element.elements();
        return transform(elements, new Function<Element, ConnectConditionDefinition>() {
            @Override
            public ConnectConditionDefinition apply(Element element) {
                return new ConnectConditionDefinition(element.attributeValue("name"), element.attributeValue("class"));
            }
        });
    }

    @Override
    public ConnectConditionClassProvider getModule() {
        return new ConnectConditionClassProvider(this.conditionClasses);
    }

    public static final class ConnectConditionClassProvider
    {
        private final List<ConnectConditionDefinition> conditionDefinitions;

        public ConnectConditionClassProvider(final Iterable<ConnectConditionDefinition> classNames)
        {
            this.conditionDefinitions = ImmutableList.copyOf(classNames);
        }

        public List<ConnectConditionDefinition> getConditionDefinitions() {
            return conditionDefinitions;
        }
    }

    public static final class ConnectConditionDefinition
    {
        private final String name;
        private final String className;

        public ConnectConditionDefinition(String name, String className)
        {
            this.name = name;
            this.className = className;
        }

        public String getName() {
            return name;
        }

        public String getClassName() {
            return className;
        }
    }
}
