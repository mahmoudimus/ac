package com.atlassian.plugin.connect.plugin.addonproperty;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyAO;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyService;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyServiceImpl;
import com.atlassian.sal.api.user.UserProfile;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.plugin.service.AddOnPropertyService.ServiceResult;
import static com.atlassian.plugin.connect.plugin.service.AddOnPropertyServiceImpl.ServiceResultImpl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddOnPropertyServiceImplTest
{
    @Mock
    private AddOnPropertyStore store;
    @Mock
    private UserProfile user;

    private final String addOnKey = "testAddon";
    private final AddOnProperty property = new AddOnProperty("testProperty", "{}");

    private AddOnPropertyService service;

    @Before
    public void init()
    {
        service = new AddOnPropertyServiceImpl(store);
    }

    @Test
    public void testGetExistingProperty() throws Exception
    {
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Option.some(property));

        Either<AddOnPropertyService.ServiceResult, AddOnProperty> result = service.getPropertyValue(user, addOnKey, addOnKey, property.getKey());
        assertTrue(result.isRight());

        assertEquals(property, result.right().get());
    }

    @Test
    public void testGetNonExistingProperty() throws Exception
    {
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Option.<AddOnProperty>none());

        Either<ServiceResult, AddOnProperty> result = service.getPropertyValue(user, addOnKey, addOnKey, property.getKey());
        assertTrue(result.isLeft());
        assertEquals(ServiceResultImpl.PROPERTY_NOT_FOUND, result.left().get());
    }

    @Test
    public void testPutNonExistingValidProperty() throws Exception
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue())).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_CREATED);

        ServiceResult result = service.setPropertyValue(user, addOnKey, addOnKey, property.getKey(), property.getValue());
        assertEquals(ServiceResultImpl.PROPERTY_CREATED, result);
    }

    @Test
    public void testPutExistingValidProperty() throws Exception
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue())).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_UPDATED);

        ServiceResult result = service.setPropertyValue(user, addOnKey, addOnKey, property.getKey(), property.getValue());
        assertEquals(ServiceResultImpl.PROPERTY_UPDATED, result);
    }

    @Test
    public void testPutInvalidPropertyWithTooLongKey() throws Exception
    {
        String tooLongKey = StringUtils.repeat(".", AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH);

        ServiceResult result = service.setPropertyValue(user, addOnKey, addOnKey, tooLongKey, property.getValue());
        assertEquals(ServiceResultImpl.KEY_TOO_LONG, result);
    }

    @Test
    public void testMaximumPropertiesExceeded() throws Exception
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue())).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_LIMIT_EXCEEDED);

        ServiceResult result = service.setPropertyValue(user, addOnKey, addOnKey, property.getKey(), property.getValue());
        assertEquals(ServiceResultImpl.MAXIMUM_PROPERTIES_EXCEEDED, result);
    }

    @Test
    public void testNoAccessToGetDifferentPluginData() throws Exception
    {
        Either<ServiceResult, AddOnProperty> result = service.getPropertyValue(user, "DIFF_PLUGIN_KEY", addOnKey, property.getKey());
        assertTrue(result.isLeft());
        assertEquals(ServiceResultImpl.ACCESS_FORBIDDEN, result.left().get());
    }

    @Test
    public void testNoAccessToPutDifferentPluginData() throws Exception
    {
        ServiceResult result = service.setPropertyValue(user, "DIFF_PLUGIN_KEY", addOnKey, property.getKey(), property.getValue());
        assertEquals(ServiceResultImpl.ACCESS_FORBIDDEN, result);
    }

    @Test
    public void testGetNoAccessWhenNotLoggedIn() throws Exception
    {
        Either<ServiceResult, AddOnProperty> result = service.getPropertyValue(null, "DIFF_PLUGIN_KEY", addOnKey, property.getKey());
        assertTrue(result.isLeft());
        assertEquals(ServiceResultImpl.NOT_LOGGED_IN, result.left().get());
    }

    @Test
    public void testSetNoAccessWhenNotLoggedIn() throws Exception
    {
        ServiceResult result = service.setPropertyValue(null, "DIFF_PLUGIN_KEY", addOnKey, property.getKey(), property.getValue());
        assertEquals(ServiceResultImpl.NOT_LOGGED_IN, result);
    }

    @Test
    public void testInvalidJson()
    {
        assertInvalidJson("[");
        assertInvalidJson("{");
    }

    private void assertInvalidJson(final String value)
    {
        ServiceResult result = service.setPropertyValue(user, addOnKey, addOnKey, property.getKey(), value);
        assertEquals(ServiceResultImpl.INVALID_FORMAT, result);
    }

    @Test
    public void testValidNullPrimitiveValue() throws Exception
    {
        assertValidJson("null");
    }

    @Test
    public void testValidBooleanValues() throws Exception
    {
        assertValidJson("true");
        assertValidJson("false");
    }

    @Test
    public void testValidNumbers() throws Exception
    {
        assertValidJson("0");
        assertValidJson("0.1");
        assertValidJson("2.0E5");
        assertValidJson("-4");
    }

    @Test
    public void testValidArray() throws Exception
    {
        assertValidJson("[]");
        assertValidJson("[true]");
        assertValidJson("[true,false]");
    }

    @Test
    public void testValidJsonValue() throws Exception
    {
        assertValidJson("{}");
        assertValidJson("{k : true}");
    }

    private void assertValidJson(String value)
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), value)).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_UPDATED);
        ServiceResult result = service.setPropertyValue(user, addOnKey, addOnKey, property.getKey(), value);
        assertEquals(ServiceResultImpl.PROPERTY_UPDATED, result);
    }
}