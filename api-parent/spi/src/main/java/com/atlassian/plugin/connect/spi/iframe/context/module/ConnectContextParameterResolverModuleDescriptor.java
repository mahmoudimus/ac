package com.atlassian.plugin.connect.spi.iframe.context.module;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.atlassian.plugin.connect.spi.module.ContextParametersValidator;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * Descriptor for context parameter extractors and validators.
 *
 * This descriptor expects XML of the form:
 *
 *     <pre>{@code
 *
 *        <root>
 *            <extractors>
 *               <extractor class="..."/>
 *               <extractor class="..."/>
 *               ...
 *            </extractors>
 *            <validators>
 *               <validator class="..."/>
 *               <validator class="..."/>
 *               ...
 *            </validators>
 *        <root>
 *
 *     }</pre>
 *
 * <p>
 *     Extractors need to be of type {@link com.atlassian.plugin.connect.spi.module.ContextParametersExtractor},
 *     and validators of type {@link com.atlassian.plugin.connect.spi.module.ContextParametersValidator}.
 *     The result is an object encapsulating both of the lists: extractors and validators.
 * </p>
 *
 */
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

        return transform(filter(subElements(container), new Predicate<Element>()
        {
            @Override
            public boolean apply(final Element input)
            {
                Attribute classAttribute = input.attribute("class");
                return input.getName().equals(elementName) && classAttribute != null && classAttribute.getValue() != null;
            }
        }), new Function<Element, String>()
        {
            @Override
            public String apply(final Element input)
            {
                return input.attribute("class").getValue();
            }
        });
    }

    private static Element subElement(Element root, final String name)
    {
        Optional<Element> result = Iterables.tryFind(subElements(root), new Predicate<Element>()
        {
            @Override
            public boolean apply(final Element input)
            {
                return input.getName().equals(name);
            }
        });

        if (result.isPresent())
        {
            return result.get();
        }
        else
        {
            throw new IllegalArgumentException("expected required element '" + name + "' below " + root.getName());
        }
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
        return transform(classNames, new Function<String, T>()
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
        // the module descriptor generic type doesn't seem to matter at all in this method, so we can cheat a little
        Object bean = moduleFactory.createModule(className, this);
        if (bean != null && type.isInstance(bean))
        {
            return type.cast(bean);
        }
        else
        {
            throw new IllegalArgumentException("Expected component of type " + type.toString() + ", got " + bean + " instead");
        }
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
