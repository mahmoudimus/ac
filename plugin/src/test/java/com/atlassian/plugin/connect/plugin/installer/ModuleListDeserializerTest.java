package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.google.common.base.Supplier;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ModuleListDeserializerTest extends TestCase
{
    @InjectMocks
    private ModuleListDeserializer moduleListDeserializer;

    @Mock
    private AvailableModuleTypes providers;
    
    @Mock
    private JsonDeserializationContext context;

    private static JsonElement testJsonElement;

    @BeforeClass
    public static void createTestJson()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("firstModuleType", "testModuleBody");
        jsonObject.addProperty("secondModuleType", "testModuleBody");
        testJsonElement = jsonObject;
    }
    
    @Test(expected = InvalidDescriptorException.class)
    public void unknownModuleTypeShouldThrowException() throws Exception
    {
        when(providers.validModuleType(anyString())).thenReturn(false);
        moduleListDeserializer.deserialize(testJsonElement, Object.class, context);
    }

    @Test
    public void deserializationReturnsCorrectNumberOfSuppliers() throws Exception
    {
        when(providers.validModuleType(anyString())).thenReturn(true);
        Map<String, Supplier<List<ModuleBean>>> map = moduleListDeserializer.deserialize(testJsonElement, Object.class, context);
        assertThat(map.size(), equalTo(2));
    }
    
}