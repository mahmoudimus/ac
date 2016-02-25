package com.atlassian.plugin.connect.plugin.auth.scope.whitelist;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.ConnectApiScopeWhitelist;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.ConnectApiScopeWhitelistModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationException;
import com.google.common.collect.Sets;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectApiScopeWhitelistModuleDescriptorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ConnectApiScopeWhitelistModuleDescriptor descriptor;

    @Mock
    private Plugin plugin;

    @Before
    public void setUp() {
        descriptor = new ConnectApiScopeWhitelistModuleDescriptor(ModuleFactory.LEGACY_MODULE_FACTORY);
    }

    @Test
    public void shouldRejectModuleWithoutKey() {
        Element element = buildModuleElement(Optional.of("/some-file.json"));
        element.remove(element.attribute("key"));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(String.format("The key is required: %s", element.asXML()));

        descriptor.init(plugin, element);
    }

    @Test
    public void shouldRejectModuleWithoutResourceAttribute() {
        Element element = buildModuleElement(Optional.empty());
        expectModuleValidationExceptionOnMissingResource(element);

        descriptor.init(plugin, element);
    }

    @Test
    public void shouldRejectModuleWithEmptyResourceAttribute() {
        Element element = buildModuleElement(Optional.of(""));
        expectModuleValidationExceptionOnMissingResource(element);

        descriptor.init(plugin, element);
    }

    @Test
    public void shouldRejectModuleReferencingMissingResource() {
        String filename = "/some-missing-file.json";
        expectedException.expect(PluginParseException.class);
        expectedException.expectMessage(String.format("Unable to load API scope whitelist resource (%s)", filename));

        Element element = buildModuleElement(Optional.of(filename));
        descriptor.init(plugin, element);
    }

    @Test
    public void shouldRejectModuleWithInvalidJsonResource() {
        String classFilename = String.format("/%s.class", getClass().getCanonicalName().replace(".", "/"));
        String jsonParsingExceptionMessage = "java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1";
        expectedException.expect(PluginParseException.class);
        expectedException.expectMessage(String.format("Unable to parse API scope whitelist (%s) - invalid JSON: %s", classFilename, jsonParsingExceptionMessage));

        when(plugin.getResource(classFilename)).thenReturn(getClass().getResource(classFilename));

        Element element = buildModuleElement(Optional.of(classFilename));

        descriptor.init(plugin, element);
    }

    @Test
    public void shouldReturnWhitelistForValidModule() {
        String classFilename = "/scope/test-whitelist.json";

        when(plugin.getResource(classFilename)).thenReturn(getClass().getResource(classFilename));

        Element element = buildModuleElement(Optional.of(classFilename));

        descriptor.init(plugin, element);
        ConnectApiScopeWhitelist whitelist = descriptor.getModule();

        assertThat(whitelist.getScopes().keySet(), equalTo(Sets.newHashSet(ScopeName.READ, ScopeName.WRITE)));
    }

    private void expectModuleValidationExceptionOnMissingResource(Element element) {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(String.format("An API scope whitelist resource must be specified via the 'resource' attribute: %s", element.asXML()));
    }

    private Element buildModuleElement(Optional<String> optionalResource) {
        Element element = new DOMElement("connect-api-scope-whitelist");
        element.addAttribute("key", "some-key");
        if (optionalResource.isPresent()) {
            element.addAttribute("resource", optionalResource.get());
        }
        return element;
    }
}
