package com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationException;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ConnectConditionClassResolverModuleDescriptorTest
{

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ConnectConditionClassResolverModuleDescriptor descriptor;

    @Mock
    private Plugin plugin;

    @Before
    public void setUp()
    {
        descriptor = new ConnectConditionClassResolverModuleDescriptor(ModuleFactory.LEGACY_MODULE_FACTORY);
    }

    @Test
    public void shouldRejectModuleWithoutKey()
    {
        Element element = buildModuleElement(Optional.empty(), Optional.of("some-class"));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(String.format("The key is required: %s", element.asXML()));

        descriptor.init(plugin, element);
    }

    @Test
    public void shouldRejectModuleWithoutClassAttribute()
    {
        Element element = buildModuleElement(Optional.of("some-key"), Optional.empty());
        expectModuleValidationExceptionOnMissingClass(element);

        descriptor.init(plugin, element);
    }

    @Test
    public void shouldRejectModuleWithEmptyClassAttribute()
    {
        Element element = buildModuleElement(Optional.of("some-key"), Optional.of(""));
        expectModuleValidationExceptionOnMissingClass(element);

        descriptor.init(plugin, element);
    }

    private void expectModuleValidationExceptionOnMissingClass(Element element)
    {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(String.format("A resolver class must be specified via the 'class' attribute: %s", element.asXML()));
    }

    private Element buildModuleElement(Optional<String> optionalKey, Optional<String> optionalClass)
    {
        Element element = new DOMElement("connect-condition-class-resolver");
        if (optionalKey.isPresent())
        {
            element.addAttribute("key", optionalKey.get());
        }
        if (optionalClass.isPresent())
        {
            element.addAttribute("class", optionalClass.get());
        }
        return element;
    }
}
