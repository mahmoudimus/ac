package com.atlassian.plugin.connect.plugin.iframe.context.module;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Options;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.atlassian.plugin.connect.spi.module.ContextParametersValidator;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
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

    private Iterable<String> extractorClasses;
    private Iterable<String> validatorClasses;
    private ConnectContextParametersResolver module;

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
        super.init(plugin, element);
        this.extractorClasses = loadClassNames(element, "extractors", "extractor");
        this.validatorClasses = loadClassNames(element, "validators", "validator");
    }

    private static Iterable<String> loadClassNames(final Element root, final String containerName, final String elementName)
    {
        Element container = subElement(root, containerName);
        return Options.flatten(Iterables.transform(subElements(container), new Function<Element, Option<String>>()
        {
            @Override
            public Option<String> apply(final Element input)
            {
                Attribute aClass = input.attribute("class");
                if (input.getName().equals(elementName) && aClass != null && aClass.getValue() != null)
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

    private static Element subElement(Element root, String name)
    {
        List<Element> subElements = subElements(root);
        for (Element subElement : subElements)
        {
            if (subElement.getName().equals(name))
            {
                return subElement;
            }
        }
        throw new IllegalArgumentException("expected required element: " + name + " below " + root.getName());
    }

    @SuppressWarnings ("unchecked")
    private static List<Element> subElements(Element root)
    {
        return root.elements();
    }

    @Override
    public void enabled()
    {
        super.enabled();
        this.module = new ConnectContextParametersResolver(
                loadClasses(extractorClasses, ContextParametersExtractor.class),
                loadClasses(validatorClasses, ContextParametersValidator.class)
        );
    }

    private <T> Iterable<T> loadClasses(final Iterable<String> classNames, final Class<T> type)
    {
        return Iterables.transform(classNames, new Function<String, T>()
        {
            @Override
            public T apply(final String input)
            {
                return createBean(input, type);
            }
        });
    }

    private <T> T createBean(String className, Class<T> type)
    {
        // the module descriptor generic type doesn't seem to matter in this method, so we can cheat a little
        ModuleDescriptor<T> moduleDescriptorOfUnknownType = (ModuleDescriptor<T>) this;
        return moduleFactory.createModule(className, moduleDescriptorOfUnknownType);
    }

    @Override
    public ConnectContextParametersResolver getModule()
    {
        return module;
    }

    @Override
    public Class<ConnectContextParametersResolver> getModuleClass()
    {
        return super.getModuleClass();
    }
}
