package com.atlassian.plugin.connect.plugin.addonproperty;

import com.atlassian.fugue.Either;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyService;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyServiceImpl;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddOnPropertyServiceImplTest
{
    @Mock
    private AddOnPropertyStore store;
    @Mock
    private ConnectAddonManager manager;

    private final String addOnKey = "testAddon";
    private final AddOnProperty property = new AddOnProperty("testProperty", "{}");


    private AddOnPropertyService service;

    @Before
    public void init()
    {
        service = new AddOnPropertyServiceImpl(store, manager);
        when(manager.getExistingAddon(addOnKey)).thenReturn(new ConnectAddonBean());
    }

    @Test
    public void testGetExistingProperty() throws Exception
    {
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Optional.of(property));

        Either<AddOnPropertyService.ServiceResultWithReason, AddOnProperty> result = service.getPropertyValue(addOnKey, addOnKey, property.getKey());
        assertTrue(result.isRight());

        assertEquals(property, result.right().get());
    }

    @Test
    public void testGetNonExistingProperty() throws Exception
    {
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Optional.<AddOnProperty>absent());

        Either<AddOnPropertyService.ServiceResultWithReason, AddOnProperty> result = service.getPropertyValue(addOnKey, addOnKey, property.getKey());
        assertTrue(result.isLeft());

        assertEquals(AddOnPropertyService.ServiceResult.PROPERTY_NOT_FOUND, result.left().get().getResult());
    }

    @Test
    public void testPutNonExistingValidProperty() throws Exception
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue())).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_CREATED);

        AddOnPropertyService.ServiceResult result = service.setPropertyValue(addOnKey, addOnKey, property.getKey(), property.getValue());
        assertEquals(AddOnPropertyService.ServiceResult.PROPERTY_CREATED, result);
    }

    @Test
    public void testPutExistingValidProperty() throws Exception
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue())).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_UPDATED);

        AddOnPropertyService.ServiceResult result = service.setPropertyValue(addOnKey, addOnKey, property.getKey(), property.getValue());
        assertEquals(AddOnPropertyService.ServiceResult.PROPERTY_UPDATED, result);
    }

    @Test
    public void testPutInvalidPropertyWithTooLongKey() throws Exception
    {
        String tooLongKey = StringUtils.repeat(".", AddOnPropertyServiceImpl.MAXIMUM_KEY_LENGTH);

        AddOnPropertyService.ServiceResult result = service.setPropertyValue(addOnKey, addOnKey, tooLongKey, property.getValue());
        assertEquals(AddOnPropertyService.ServiceResult.KEY_TOO_LONG, result);
    }

    @Test
    public void testMaximumPropertiesExceeded() throws Exception
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue())).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_LIMIT_EXCEEDED);

        AddOnPropertyService.ServiceResult result = service.setPropertyValue(addOnKey, addOnKey, property.getKey(), property.getValue());
        assertEquals(AddOnPropertyService.ServiceResult.MAXIMUM_PROPERTIES_EXCEEDED, result);
    }

    @Test
    public void testValueTooBig() throws Exception
    {
        final String tooBigValue = StringUtils.repeat(" ", AddOnPropertyServiceImpl.MAXIMUM_VALUE_LENGTH);
        AddOnPropertyService.ServiceResult result = service.setPropertyValue(addOnKey, addOnKey, property.getKey(),tooBigValue);
        assertEquals(AddOnPropertyService.ServiceResult.VALUE_TOO_BIG, result);
    }

    @Test
    public void testNoAccessToGetDifferentPluginData() throws Exception
    {
        Either<AddOnPropertyService.ServiceResultWithReason, AddOnProperty> result = service.getPropertyValue("DIFF_PLUGIN_KEY", addOnKey, property.getKey());
        assertTrue(result.isLeft());
        assertEquals(AddOnPropertyService.ServiceResult.ACCESS_FORBIDDEN, result.left().get().getResult());
    }
    @Test
    public void testNoAccessToPutDifferentPluginData() throws Exception
    {
        AddOnPropertyService.ServiceResult result = service.setPropertyValue("DIFF_PLUGIN_KEY", addOnKey, property.getKey(), property.getValue());
        assertEquals(AddOnPropertyService.ServiceResult.ACCESS_FORBIDDEN, result);
    }

    @Test
    public void testValidJsons() throws Exception
    {
        assertValidJson("null");
        assertValidJson("true");
        assertValidJson("false");
        assertValidJson("0");
        assertValidJson("{}");
    }

    @Test
    public void testInvalidJson()
    {
        AddOnPropertyService.ServiceResult result = service.setPropertyValue(addOnKey, addOnKey, property.getKey(), "[");
        assertEquals(AddOnPropertyService.ServiceResult.INVALID_FORMAT, result);
    }

    private void assertValidJson(String value)
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), value)).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_UPDATED);
        AddOnPropertyService.ServiceResult result = service.setPropertyValue(addOnKey, addOnKey, property.getKey(), value);
        assertEquals(AddOnPropertyService.ServiceResult.PROPERTY_UPDATED, result);
    }
}