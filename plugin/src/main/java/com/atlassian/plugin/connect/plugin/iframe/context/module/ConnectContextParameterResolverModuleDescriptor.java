package com.atlassian.plugin.connect.plugin.iframe.context.module;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Options;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.atlassian.plugin.connect.spi.module.ContextParametersValidator;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.module.ModuleClassNotFoundException;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.List;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;

public final class ConnectContextParameterResolverModuleDescriptor extends AbstractModuleDescriptor<ConnectContextParameterResolverModuleDescriptor.ConnectContextParametersResolver>
{
    private Iterable<ContextParametersExtractor> extractors;
    private Iterable<ContextParametersValidator> validators;

    public static final class ConnectContextParametersResolver
    {
        private final List<ContextParametersExtractor> extractors;
        private final List<ContextParametersValidator> validators;

        public ConnectContextParametersResolver(final Iterable<ContextParametersExtractor> extractors, final Iterable<ContextParametersValidator> validators)
        {
            this.extractors = ImmutableList.copyOf(extractors);
            this.validators = ImmutableList.copyOf(validators);
        }

        public List<ContextParametersExtractor> getExtractors()
        {
            return extractors;
        }

        public List<ContextParametersValidator> getValidators()
        {
            return validators;
        }
    }

    public ConnectContextParameterResolverModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        new ModuleInitiator(plugin, element).init();
    }

    private class ModuleInitiator
    {
        private final Element root;
        private final Plugin plugin;

        public ModuleInitiator(final Plugin plugin, final Element root)
        {
            this.root = root;
            this.plugin = plugin;
        }

        public void init()
        {
            extractors = loadComponents("extractors", ContextParametersExtractor.class);
            validators = loadComponents("validators", ContextParametersValidator.class);
        }

        private <T> Iterable<T> loadComponents(String componentName, final Class<T> componentClass)
        {
            Element subElement = subElement(root, componentName);
            Iterable<String> classNames = loadClassNames(subElement);
            return Iterables.transform(classNames, new Function<String, T>()
            {
                @Override
                public T apply(final String input)
                {
                    return createBean(input, componentClass);
                }
            });
        }

        private <T> T createBean(String className, Class<T> type)
        {
            try
            {
                ContainerManagedPlugin cmPlugin = (ContainerManagedPlugin) plugin;
                Class<Object> clazz = plugin.loadClass(className, null);
                Object bean = cmPlugin.getContainerAccessor().createBean(clazz);
                return type.cast(bean);
            }
            catch (ClassNotFoundException e)
            {
                throw new ModuleClassNotFoundException(name, getPluginKey(), getKey(), e, "Couldn't find class " + className);
            }
        }

        private Iterable<String> loadClassNames(Element container)
        {
            return Options.flatten(Iterables.transform(subElements(container), new Function<Element, Option<String>>()
            {

                @Override
                public Option<String> apply(final Element input)
                {
                    Attribute aClass = input.attribute("class");
                    if (aClass != null && aClass.getValue() != null)
                    {
                        return some(aClass.getValue());
                    }
                    else
                    {
                        return none();
                    }
                }
            }));
        }

        private Element subElement(Element ofElement, String name)
        {
            List<Element> subElements = ofElement.elements();
            for (Element subElement : subElements)
            {
                if (subElement.getName().equals(name))
                {
                    return subElement;
                }
            }
            throw new IllegalArgumentException("expected required element: " + name + " below " + ofElement.getName());
        }

        private List<Element> subElements(Element ofElement)
        {
            return ofElement.elements();
        }
    }

    @Override
    public ConnectContextParametersResolver getModule()
    {
        return new ConnectContextParametersResolver(extractors, validators);
    }
}
