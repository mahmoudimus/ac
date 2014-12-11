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

        Either<AddOnProperty, Iterable<AddOnPropertyService.ValidationErrorWithReason>> result = service.getPropertyValue(addOnKey, property.getKey());
        assertTrue(result.isLeft());

        assertEquals(property, result.left().get());
    }

    @Test
    public void testGetNonExistingProperty() throws Exception
    {
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Optional.<AddOnProperty>absent());

        Either<AddOnProperty, Iterable<AddOnPropertyService.ValidationErrorWithReason>> result = service.getPropertyValue(addOnKey, property.getKey());
        assertTrue(result.isRight());

        assertEquals(AddOnPropertyService.OperationStatus.PROPERTY_NOT_FOUND, result.right().get().iterator().next().getError());
    }

    @Test
    public void testPutNonExistingValidProperty() throws Exception
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue())).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_CREATED);

        AddOnPropertyService.OperationStatus result = service.setPropertyValue(addOnKey, property.getKey(), property.getValue());
        assertEquals(AddOnPropertyService.OperationStatus.PROPERTY_CREATED, result);
    }

    @Test
    public void testPutExistingValidProperty() throws Exception
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue())).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_UPDATED);

        AddOnPropertyService.OperationStatus result = service.setPropertyValue(addOnKey, property.getKey(), property.getValue());
        assertEquals(AddOnPropertyService.OperationStatus.PROPERTY_UPDATED, result);
    }

    @Test
    public void testPutInvalidPropertyWithTooLongKey() throws Exception
    {
        String tooLongKey = StringUtils.repeat(".", AddOnPropertyServiceImpl.MAXIMUM_KEY_LENGTH);

        AddOnPropertyService.OperationStatus result = service.setPropertyValue(addOnKey, tooLongKey, property.getValue());
        assertEquals(AddOnPropertyService.OperationStatus.KEY_TOO_LONG, result);
    }

    @Test
    public void testMaximumPropertiesExceeded() throws Exception
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue())).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_LIMIT_EXCEEDED);
        service.setPropertyValue(addOnKey, property.getKey(), property.getValue());

        AddOnPropertyService.OperationStatus result = service.setPropertyValue(addOnKey, property.getKey(), property.getValue());
        assertEquals(AddOnPropertyService.OperationStatus.MAXIMUM_PROPERTIES_EXCEEDED, result);
    }

    @Test
    public void testValueTooBig() throws Exception
    {
        final String tooBigValue = StringUtils.repeat(" ", AddOnPropertyServiceImpl.MAXIMUM_VALUE_LENGTH);
        AddOnPropertyService.OperationStatus result = service.setPropertyValue(addOnKey,property.getKey(),tooBigValue);
        assertEquals(AddOnPropertyService.OperationStatus.VALUE_TOO_BIG, result);
    }
}