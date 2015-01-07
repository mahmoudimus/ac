package com.atlassian.plugin.connect.plugin.addonproperty;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyAO;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyIterable;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyStore;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyService;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyServiceImpl;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

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
    @Mock
    private UserManager userManager;
    @Mock
    private ConnectAddonRegistry connectAddonRegistry;

    private final String addOnKey = "testAddon";
    private final AddOnProperty property = new AddOnProperty("testProperty", "{}");
    public static final UserKey userKey = new UserKey("userkey");

    private AddOnPropertyService service;

    @Before
    public void init()
    {
        service = new AddOnPropertyServiceImpl(store, userManager, connectAddonRegistry);
        when(user.getUserKey()).thenReturn(userKey);
        when(connectAddonRegistry.hasAddonWithKey(addOnKey)).thenReturn(true);
    }

    private void testGetExistingProperty(String sourcePlugin)
    {
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Option.some(property));

        Either<ServiceResult, AddOnProperty> result = service.getPropertyValue(user, sourcePlugin, addOnKey, property.getKey());
        assertTrue(result.isRight());

        assertEquals(property, result.right().get());
    }

    @Test
    public void testGetExistingPropertyWhenPlugin() throws Exception
    {
        testGetExistingProperty(addOnKey);
    }

    @Test
    public void testGetExistingPropertyWhenSysAdmin() throws Exception
    {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testGetExistingProperty(null);
    }

    private void testGetNonExistingProperty(String sourcePlugin)
    {
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Option.<AddOnProperty>none());

        Either<ServiceResult, AddOnProperty> result = service.getPropertyValue(user, sourcePlugin, addOnKey, property.getKey());
        assertTrue(result.isLeft());
        assertEquals(ServiceResultImpl.PROPERTY_NOT_FOUND, result.left().get());
    }

    @Test
    public void testGetNonExistingPropertyWhenPlugin() throws Exception
    {
        testGetNonExistingProperty(addOnKey);
    }

    @Test
    public void testGetNonExistingPropertyWhenSysAdmin() throws Exception
    {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testGetNonExistingProperty(null);
    }

    private void testPutNonExistingValidProperty(final String sourcePluginKey)
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue())).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_CREATED);
        ServiceResult result = service.setPropertyValue(user, sourcePluginKey, addOnKey, property.getKey(), property.getValue());
        assertEquals(ServiceResultImpl.PROPERTY_CREATED, result);
    }

    @Test
    public void testPutNonExistingValidPropertyWhenPlugin() throws Exception
    {
        testPutNonExistingValidProperty(addOnKey);
    }

    @Test
    public void testPutNonExistingValidPropertyWhenSysAdmin() throws Exception
    {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testPutNonExistingValidProperty(null);
    }

    private void testPutExistingValidProperty(final String sourcePluginKey)
    {
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue())).thenReturn(AddOnPropertyStore.PutResult.PROPERTY_UPDATED);

        ServiceResult result = service.setPropertyValue(user, sourcePluginKey, addOnKey, property.getKey(), property.getValue());
        assertEquals(ServiceResultImpl.PROPERTY_UPDATED, result);
    }

    @Test
    public void testPutExistingValidPropertyWhenPlugin() throws Exception
    {
        testPutExistingValidProperty(addOnKey);
    }

    @Test
    public void testPutExistingValidPropertyWhenSysAdmin() throws Exception
    {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testPutExistingValidProperty(null);
    }

    private void testDeleteExistingProperty(final String sourcePluginKey)
    {
        when(store.deletePropertyValue(addOnKey, property.getKey())).thenReturn(AddOnPropertyStore.DeleteResult.PROPERTY_DELETED);

        ServiceResult result = service.deletePropertyValue(user, sourcePluginKey, addOnKey, property.getKey());
        assertEquals(ServiceResultImpl.PROPERTY_DELETED, result);
    }

    @Test
    public void testDeleteExistingPropertyWhenPlugin() throws Exception
    {
        testDeleteExistingProperty(addOnKey);
    }

    @Test
    public void testDeleteExistingPropertyWhenSysAdmin() throws Exception
    {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testDeleteExistingProperty(null);
    }

    private void testDeleteNonExistingProperty(final String sourcePluginKey)
    {
        when(store.deletePropertyValue(addOnKey, property.getKey())).thenReturn(AddOnPropertyStore.DeleteResult.PROPERTY_NOT_FOUND);

        ServiceResult result = service.deletePropertyValue(user, sourcePluginKey, addOnKey, property.getKey());
        assertEquals(ServiceResultImpl.PROPERTY_NOT_FOUND, result);
    }

    @Test
    public void testDeleteNonExistingPropertyWhenPlugin() throws Exception
    {
        testDeleteNonExistingProperty(addOnKey);
    }

    @Test
    public void testDeleteNonExistingPropertyWhenSysAdmin() throws Exception
    {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testDeleteNonExistingProperty(null);
    }

    private void testListProperties(final String sourcePluginKey)
    {
        AddOnPropertyIterable emptyIterable = new AddOnPropertyIterable(Collections.<AddOnProperty>emptyList());
        when(store.getAllPropertiesForAddOnKey(addOnKey)).thenReturn(emptyIterable);
        Either<ServiceResult, AddOnPropertyIterable> result = service.getAddOnProperties(user, sourcePluginKey, addOnKey);

        assertEquals(emptyIterable, result.right().get());
    }

    @Test
    public void testListPropertiesWhenPlugin() throws Exception
    {
        testListProperties(addOnKey);
    }

    @Test
    public void testListPropertiesWhenSysAdmin() throws Exception
    {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testListProperties(null);
    }

    @Test
    public void testGetInvalidPropertyWithTooLongKey() throws Exception
    {
        String tooLongKey = StringUtils.repeat(".", AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH + 1);

        Either<ServiceResult, AddOnProperty> result = service.getPropertyValue(user, addOnKey, addOnKey, tooLongKey);
        assertTrue(result.isLeft());
        assertEquals(ServiceResultImpl.KEY_TOO_LONG, result.left().get());
    }

    @Test
    public void testPutInvalidPropertyWithTooLongKey() throws Exception
    {
        String tooLongKey = StringUtils.repeat(".", AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH + 1);

        ServiceResult result = service.setPropertyValue(user, addOnKey, addOnKey, tooLongKey, property.getValue());
        assertEquals(ServiceResultImpl.KEY_TOO_LONG, result);
    }

    @Test
    public void testDeleteInvalidPropertyWithTooLongKey() throws Exception
    {
        String tooLongKey = StringUtils.repeat(".", AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH + 1);

        ServiceResult result = service.deletePropertyValue(user, addOnKey, addOnKey, tooLongKey);
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
        assertEquals(ServiceResultImpl.ACCESS_TO_OTHER_DATA_FORBIDDEN, result.left().get());
    }

    @Test
    public void testNoAccessToPutDifferentPluginData() throws Exception
    {
        ServiceResult result = service.setPropertyValue(user, "DIFF_PLUGIN_KEY", addOnKey, property.getKey(), property.getValue());
        assertEquals(ServiceResultImpl.ACCESS_TO_OTHER_DATA_FORBIDDEN, result);
    }

    @Test
    public void testNoAccessToDeleteDifferentPluginData() throws Exception
    {
        ServiceResult result = service.deletePropertyValue(user, "DIFF_PLUGIN_KEY", addOnKey, property.getKey());
        assertEquals(ServiceResultImpl.ACCESS_TO_OTHER_DATA_FORBIDDEN, result);
    }

    @Test
    public void testNoAccessToListDifferentPluginData() throws Exception
    {
        Either<ServiceResult, AddOnPropertyIterable> result = service.getAddOnProperties(user, "DIFF_PLUGIN_KEY", addOnKey);
        assertTrue(result.isLeft());
        assertEquals(ServiceResultImpl.ACCESS_TO_OTHER_DATA_FORBIDDEN, result.left().get());
    }

    @Test
    public void testGetNoAccessWhenNotLoggedIn() throws Exception
    {
        Either<ServiceResult, AddOnProperty> result = service.getPropertyValue(null, "DIFF_PLUGIN_KEY", addOnKey, property.getKey());
        assertTrue(result.isLeft());
        assertEquals(ServiceResultImpl.NOT_AUTHENTICATED, result.left().get());
    }

    @Test
    public void testSetNoAccessWhenNotLoggedIn() throws Exception
    {
        ServiceResult result = service.setPropertyValue(null, "DIFF_PLUGIN_KEY", addOnKey, property.getKey(), property.getValue());
        assertEquals(ServiceResultImpl.NOT_AUTHENTICATED, result);
    }

    @Test
    public void testDeleteNoAccessWhenLoggedIn() throws Exception
    {
        ServiceResult result = service.deletePropertyValue(null, "DIFF_PLUGIN_KEY", addOnKey, property.getKey());
        assertEquals(ServiceResultImpl.NOT_AUTHENTICATED, result);
    }
    @Test
    public void testListNoAccessWhenLoggedIn() throws Exception
    {
        Either<ServiceResult, AddOnPropertyIterable> result = service.getAddOnProperties(null, "DIFF_PLUGIN_KEY", addOnKey);
        assertTrue(result.isLeft());
        assertEquals(ServiceResultImpl.NOT_AUTHENTICATED, result.left().get());
    }

    @Test
    public void testAddOnNotFoundWhenPluginNotInstalledAndSysAdmin() throws Exception
    {
        when(connectAddonRegistry.hasAddonWithKey(addOnKey)).thenReturn(false);
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);

        Either<ServiceResult, AddOnProperty> result = service.getPropertyValue(user, null, addOnKey, "");
        assertTrue(result.isLeft());
        assertEquals(ServiceResultImpl.ADD_ON_NOT_FOUND, result.left().get());
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
        assertEquals(ServiceResultImpl.INVALID_PROPERTY_VALUE, result);
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
        assertValidJson("[true, { \"hello\": 3 }]");
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