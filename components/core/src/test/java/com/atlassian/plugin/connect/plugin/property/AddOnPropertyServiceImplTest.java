package com.atlassian.plugin.connect.plugin.property;

import java.util.Collections;
import java.util.Optional;

import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.property.AddOnPropertyService.DeleteServiceResult;
import com.atlassian.plugin.connect.plugin.property.AddOnPropertyService.PutServiceResult;
import com.atlassian.plugin.connect.plugin.property.AddOnPropertyStore.PutResultWithOptionalProperty;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import com.google.common.base.Function;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.atlassian.plugin.connect.plugin.property.AddOnPropertyService.OperationStatus;
import static com.atlassian.plugin.connect.plugin.property.AddOnPropertyServiceImpl.OperationStatusImpl;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddOnPropertyServiceImplTest
{
    public static final UserKey userKey = new UserKey("userkey");

    private final String addOnKey = "testAddon";
    private static final JsonNode EMPTY_OBJECT = JsonCommon.parseStringToJson("{}").get();
    private final AddOnProperty property = new AddOnProperty("testProperty", EMPTY_OBJECT, 0);

    @Mock
    private AddOnPropertyStore store;
    @Mock
    private UserProfile user;
    @Mock
    private UserManager userManager;
    @Mock
    private ConnectAddonRegistry connectAddonRegistry;

    private AddOnPropertyService service;
    private Function<OperationStatus, Void> mockFunction;
    private Function<AddOnPropertyService.PutOperationStatus, Void> mockPutFunction;

    @Before
    public void init()
    {
        service = new AddOnPropertyServiceImpl(store, userManager, connectAddonRegistry);
        when(user.getUserKey()).thenReturn(userKey);
        when(connectAddonRegistry.hasAddonWithKey(addOnKey)).thenReturn(true);
        mockFunction = getMockForFunction();
        mockPutFunction = getMockForPutFunction();
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

        AddOnPropertyService.GetServiceResult result = service.getPropertyValue(user, addOnKey, addOnKey, tooLongKey);
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.KEY_TOO_LONG);
    }

    @Test
    public void testPutInvalidPropertyWithTooLongKey() throws Exception
    {
        String tooLongKey = StringUtils.repeat(".", AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH + 1);

        PutServiceResult<Void> result = service.setPropertyValueIfConditionSatisfied(user, addOnKey, addOnKey, tooLongKey, property.getValue().toString(), alwaysTrue());
        result.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.KEY_TOO_LONG);
    }

    @Test
    public void testDeleteInvalidPropertyWithTooLongKey() throws Exception
    {
        String tooLongKey = StringUtils.repeat(".", AddOnPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH + 1);

        DeleteServiceResult<Void> result = service.deletePropertyValueIfConditionSatisfied(user, addOnKey, addOnKey, tooLongKey, alwaysTrue());
        result.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.KEY_TOO_LONG);
    }

    @Test
    public void testMaximumPropertiesExceeded() throws Exception
    {
        mockExecuteInTransaction();
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Optional.of(property));
        PutResultWithOptionalProperty mockedResult = new PutResultWithOptionalProperty(AddOnPropertyStore.PutResult.PROPERTY_LIMIT_EXCEEDED, Optional.empty());
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue().toString())).thenReturn(mockedResult);

        PutServiceResult<Void> result = service.setPropertyValueIfConditionSatisfied(user, addOnKey, addOnKey, property.getKey(), property.getValue().toString(), alwaysTrue());
        result.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.MAXIMUM_PROPERTIES_EXCEEDED);
    }

    @Test
    public void testNoAccessToGetDifferentPluginData() throws Exception
    {
        AddOnPropertyService.GetServiceResult result = service.getPropertyValue(user, "DIFF_PLUGIN_KEY", addOnKey, property.getKey());
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
    }

    @Test
    public void testNoAccessToPutDifferentPluginData() throws Exception
    {
        PutServiceResult<Void> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(user, "DIFF_PLUGIN_KEY", addOnKey, property.getKey(), property.getValue().toString(), alwaysTrue());
        foldableServiceResult.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
    }

    @Test
    public void testNoAccessToDeleteDifferentPluginData() throws Exception
    {
        DeleteServiceResult<Void> foldableServiceResult = service.deletePropertyValueIfConditionSatisfied(user, "DIFF_PLUGIN_KEY", addOnKey, property.getKey(), alwaysTrue());
        foldableServiceResult.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
    }

    @Test
    public void testNoAccessToListDifferentPluginData() throws Exception
    {
        AddOnPropertyService.GetAllServiceResult result = service.getAddOnProperties(user, "DIFF_PLUGIN_KEY", addOnKey);
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
    }

    @Test
    public void testGetNoAccessWhenNotLoggedIn() throws Exception
    {
        AddOnPropertyService.GetServiceResult result = service.getPropertyValue(null, "DIFF_PLUGIN_KEY", addOnKey, property.getKey());
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.NOT_AUTHENTICATED);
    }

    @Test
    public void testSetNoAccessWhenNotLoggedIn() throws Exception
    {
        PutServiceResult<Void> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(null, "DIFF_PLUGIN_KEY", addOnKey, property.getKey(), property.getValue().toString(), alwaysTrue());
        foldableServiceResult.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.NOT_AUTHENTICATED);
    }

    @Test
    public void testDeleteNoAccessWhenLoggedIn() throws Exception
    {
        DeleteServiceResult<Void> foldableServiceResult = service.deletePropertyValueIfConditionSatisfied(null, "DIFF_PLUGIN_KEY", addOnKey, property.getKey(), alwaysTrue());
        foldableServiceResult.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.NOT_AUTHENTICATED);
    }

    @Test
    public void testListNoAccessWhenLoggedIn() throws Exception
    {
        AddOnPropertyService.GetAllServiceResult result = service.getAddOnProperties(null, "DIFF_PLUGIN_KEY", addOnKey);
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.NOT_AUTHENTICATED);
    }

    @Test
    public void testAddOnNotFoundWhenPluginNotInstalledAndSysAdmin() throws Exception
    {
        when(connectAddonRegistry.hasAddonWithKey(addOnKey)).thenReturn(false);
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);

        AddOnPropertyService.GetServiceResult result = service.getPropertyValue(user, null, addOnKey, "");
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
    }

    @Test
    public void testInvalidJson()
    {
        mockExecuteInTransaction();
        assertInvalidJson("[");
        assertInvalidJson("{");
    }

    @Test
    public void testInvalidJsonString()
    {
        assertInvalidJson("asdadf");
        assertInvalidJson("\"asdadf");
        assertInvalidJson("asdadf\"");
    }

    @Test
    public void testValidNullPrimitiveValue() throws Exception
    {
        mockExecuteInTransaction();
        assertValidJson("null");
    }

    @Test
    public void testValidBooleanValues() throws Exception
    {
        mockExecuteInTransaction();
        assertValidJson("true");
        assertValidJson("false");
    }

    @Test
    public void testValidNumbers() throws Exception
    {
        mockExecuteInTransaction();
        assertValidJson("0");
        assertValidJson("0.1");
        assertValidJson("2.0E5");
        assertValidJson("-4");
    }

    @Test
    public void testValidStrings() throws Exception
    {
        mockExecuteInTransaction();
        assertValidJson("\"asdasd\"");
    }

    @Test
    public void testValidArray() throws Exception
    {
        mockExecuteInTransaction();
        assertValidJson("[]");
        assertValidJson("[true]");
        assertValidJson("[true,false]");
        assertValidJson("[true, { \"hello\": 3 }]");
    }

    @Test
    public void testValidJsonValue() throws Exception
    {
        mockExecuteInTransaction();
        assertValidJson("{}");
        assertValidJson("{ \"k\" : true}");
    }

    @Test
    public void testPreconditionFailedCalledWhenTestFunctionReturnedFalse()
    {
        mockExecuteInTransaction();
        final Object obj = new Object();
        PutServiceResult<Object> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(user, addOnKey, addOnKey, property.getKey(), "0", new Function<Optional<AddOnProperty>, AddOnPropertyService.ServiceConditionResult<Object>>()
        {
            @Override
            public AddOnPropertyService.ServiceConditionResult<Object> apply(final Optional<AddOnProperty> input)
            {
                return AddOnPropertyService.ServiceConditionResult.FAILURE_WITH_OBJECT(obj);
            }
        });
        Function<Object, Object> mockFunction = (Function<Object, Object>) mock(Function.class);
        foldableServiceResult.fold(mockFunction, null, null);
        verify(mockFunction).apply(obj);
    }

    private void testGetExistingProperty(String sourcePlugin)
    {
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Optional.of(property));
        AddOnPropertyService.GetServiceResult result = service.getPropertyValue(user, sourcePlugin, addOnKey, property.getKey());
        Function<AddOnProperty, Void> mockFunction = mock(Function.class);
        result.fold(null, mockFunction);
        verify(mockFunction).apply(property);
    }

    private void testGetNonExistingProperty(String sourcePlugin)
    {
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Optional.empty());
        AddOnPropertyService.GetServiceResult result = service.getPropertyValue(user, sourcePlugin, addOnKey, property.getKey());
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.PROPERTY_NOT_FOUND);
    }

    private void testPutNonExistingValidProperty(final String sourcePluginKey)
    {
        mockExecuteInTransaction();
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Optional.empty());

        PutResultWithOptionalProperty mockedResult = new PutResultWithOptionalProperty(AddOnPropertyStore.PutResult.PROPERTY_CREATED, Optional.of(property));
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue().toString())).thenReturn(mockedResult);

        PutServiceResult<Void> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(user, sourcePluginKey, addOnKey, property.getKey(), property.getValue().toString(), alwaysTrue());

        foldableServiceResult.fold(null, null, mockPutFunction);

        verify(mockPutFunction).apply(argThat(hasServiceResult(OperationStatusImpl.PROPERTY_CREATED)));
    }

    private void testPutExistingValidProperty(final String sourcePluginKey)
    {
        mockExecuteInTransaction();
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Optional.of(property));
        PutResultWithOptionalProperty mockedResult = new PutResultWithOptionalProperty(AddOnPropertyStore.PutResult.PROPERTY_UPDATED, Optional.of(property));
        when(store.setPropertyValue(addOnKey, property.getKey(), property.getValue().toString())).thenReturn(mockedResult);

        final PutServiceResult<Void> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(user, sourcePluginKey, addOnKey, property.getKey(), property.getValue().toString(), alwaysTrue());

        foldableServiceResult.fold(null, null, mockPutFunction);
        verify(mockPutFunction).apply(argThat(hasServiceResult(OperationStatusImpl.PROPERTY_UPDATED)));
    }

    private void testDeleteExistingProperty(final String sourcePluginKey)
    {
        mockExecuteInTransaction();
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Optional.of(new AddOnProperty("", EMPTY_OBJECT, 0)));

        DeleteServiceResult<Void> foldableServiceResult = service.deletePropertyValueIfConditionSatisfied(user, sourcePluginKey, addOnKey, property.getKey(), alwaysTrue());
        foldableServiceResult.fold(null, null, mockFunction);
        verify(mockFunction).apply(OperationStatusImpl.PROPERTY_DELETED);
    }

    private void testDeleteNonExistingProperty(final String sourcePluginKey)
    {
        mockExecuteInTransaction();
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Optional.empty());

        DeleteServiceResult<Void> deleteServiceResult = service.deletePropertyValueIfConditionSatisfied(user, sourcePluginKey, addOnKey, property.getKey(), alwaysTrue());
        deleteServiceResult.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.PROPERTY_NOT_FOUND);
    }

    private void testListProperties(final String sourcePluginKey)
    {
        AddOnPropertyIterable emptyIterable = new AddOnPropertyIterable(Collections.<AddOnProperty>emptyList());
        when(store.getAllPropertiesForAddOnKey(addOnKey)).thenReturn(emptyIterable);

        Function<AddOnPropertyIterable, Void> mockFunction = mock(Function.class);
        AddOnPropertyService.GetAllServiceResult result = service.getAddOnProperties(user, sourcePluginKey, addOnKey);
        result.fold(null, mockFunction);
        verify(mockFunction).apply(emptyIterable);
    }

    private void assertValidJson(String value)
    {
        PutResultWithOptionalProperty mockedResult = new PutResultWithOptionalProperty(AddOnPropertyStore.PutResult.PROPERTY_UPDATED, Optional.of(
            property));
        when(store.setPropertyValue(addOnKey, property.getKey(), value)).thenReturn(mockedResult);
        when(store.getPropertyValue(addOnKey, property.getKey())).thenReturn(Optional.of(new AddOnProperty(
            property.getKey(), property.getValue(), 0)));
        PutServiceResult<Void> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(user, addOnKey, addOnKey, property.getKey(), value, alwaysTrue());

        Function<AddOnPropertyService.PutOperationStatus, Void> mockPutFunction = getMockForPutFunction();
        foldableServiceResult.fold(null, null, mockPutFunction);
        verify(mockPutFunction).apply(argThat(hasServiceResult(OperationStatusImpl.PROPERTY_UPDATED)));
    }

    private void assertInvalidJson(final String value)
    {
        PutServiceResult<Void> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(user, addOnKey, addOnKey, property.getKey(), value, alwaysTrue());
        Function<OperationStatus, Void> mockFunction = getMockForFunction();
        foldableServiceResult.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.INVALID_PROPERTY_VALUE);
    }

    private Function<Optional<AddOnProperty>, AddOnPropertyService.ServiceConditionResult<Void>> alwaysTrue()
    {
        return new Function<Optional<AddOnProperty>, AddOnPropertyService.ServiceConditionResult<Void>>()
        {
            @Override
            public AddOnPropertyService.ServiceConditionResult<Void> apply(final Optional<AddOnProperty> input)
            {
                return AddOnPropertyService.ServiceConditionResult.SUCCESS();
            }
        };
    }

    private void mockExecuteInTransaction()
    {
        when(store.executeInTransaction(any(AddOnPropertyStore.TransactionAction.class))).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                Object[] arguments = invocationOnMock.getArguments();
                AddOnPropertyStore.TransactionAction c = (AddOnPropertyStore.TransactionAction) arguments[0];
                return c.call();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Function<OperationStatus, Void> getMockForFunction()
    {
        return (Function<OperationStatus, Void>) mock(Function.class);
    }

    @SuppressWarnings("unchecked")
    private Function<AddOnPropertyService.PutOperationStatus, Void> getMockForPutFunction()
    {
        return (Function<AddOnPropertyService.PutOperationStatus, Void>) mock(Function.class);
    }

    private ArgumentMatcher<AddOnPropertyService.PutOperationStatus> hasServiceResult(final OperationStatus result)
    {
        return new ArgumentMatcher<AddOnPropertyService.PutOperationStatus>()
        {
            @Override
            public boolean matches(final Object o)
            {
                if (o instanceof AddOnPropertyService.PutOperationStatus)
                {
                    AddOnPropertyService.PutOperationStatus other = (AddOnPropertyService.PutOperationStatus) o;
                    return result == other.getBase();
                }
                return false;
            }
        };
    }
}