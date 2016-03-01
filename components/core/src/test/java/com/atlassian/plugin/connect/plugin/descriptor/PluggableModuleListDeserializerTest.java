package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.plugin.lifecycle.ConnectModuleProviderModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.base.Supplier;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.test.matcher.UnitTestMatchers.predicateThatWillMatch;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluggableModuleListDeserializerTest extends TestCase {

    private static final String MODULE_TYPE = "firstModuleType";

    @InjectMocks
    private PluggableModuleListDeserializer moduleListDeserializer;

    @Mock
    private PluginAccessor pluginAccessor;

    @Mock
    private JsonDeserializationContext context;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static JsonElement testJsonElement;

    @BeforeClass
    public static void createTestJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(MODULE_TYPE, "testModuleBody");
        jsonObject.addProperty("secondModuleType", "testModuleBody");
        testJsonElement = jsonObject;
    }

    @Test
    public void unknownModuleTypeShouldThrowException() throws Exception {
        when(pluginAccessor.getModules(argThat(predicateThatWillMatch(new ConnectModuleProviderModuleDescriptor(mock(ModuleFactory.class))))))
                .thenReturn(Collections.emptyList());

        Map<String, Supplier<List<ModuleBean>>> moduleBeanListSuppliers
                = moduleListDeserializer.deserialize(testJsonElement, Object.class, context);
        Supplier<List<ModuleBean>> moduleBeanListSupplier = moduleBeanListSuppliers.get(MODULE_TYPE);

        expectedException.expect(ConnectModuleValidationRuntimeException.class);
        expectedException.expectMessage(String.format("%s: No provider found for module type %s referenced in the descriptor",
                ConnectModuleValidationException.class.getName(), MODULE_TYPE));
        moduleBeanListSupplier.get();
    }

    @Test
    public void deserializationReturnsCorrectNumberOfSuppliers() throws Exception {
        Map<String, Supplier<List<ModuleBean>>> map = moduleListDeserializer.deserialize(testJsonElement, Object.class, context);
        assertThat(map.size(), equalTo(2));
    }
}
