package com.atlassian.plugin.connect.plugin.property;

import com.atlassian.plugin.connect.api.property.AddonProperty;
import com.atlassian.plugin.connect.api.property.AddonPropertyIterable;
import com.atlassian.plugin.connect.api.property.AddonPropertyService;
import com.atlassian.plugin.connect.api.property.AddonPropertyService.DeleteServiceResult;
import com.atlassian.plugin.connect.api.property.AddonPropertyService.PutServiceResult;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.property.AddonPropertyStore.PutResultWithOptionalProperty;
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
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static com.atlassian.plugin.connect.api.property.AddonPropertyService.OperationStatus;
import static com.atlassian.plugin.connect.plugin.property.AddonPropertyServiceImpl.OperationStatusImpl;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddonPropertyServiceImplTest {
    public static final UserKey userKey = new UserKey("userkey");

    private final String addonKey = "testAddon";
    private static final JsonNode EMPTY_OBJECT = JsonCommon.parseStringToJson("{}").get();
    private final AddonProperty property = new AddonProperty("testProperty", EMPTY_OBJECT, 0);

    @Mock
    private AddonPropertyStore store;
    @Mock
    private UserProfile user;
    @Mock
    private UserManager userManager;
    @Mock
    private ConnectAddonRegistry connectAddonRegistry;
    @Mock
    private Function<Object, Object> objectObjectFunction;
    @Mock
    private Function<AddonProperty, Void> addonPropertyVoidFunction;
    @Mock
    private Function<AddonPropertyIterable, Void> addonPropertyIterableVoidFunction;

    private AddonPropertyService service;
    private Function<OperationStatus, Void> mockFunction;
    private Function<AddonPropertyService.PutOperationStatus, Void> mockPutFunction;

    @Before
    public void init() {
        service = new AddonPropertyServiceImpl(store, userManager, connectAddonRegistry);
        when(user.getUserKey()).thenReturn(userKey);
        when(connectAddonRegistry.hasAddonWithKey(addonKey)).thenReturn(true);
        mockFunction = getMockForFunction();
        mockPutFunction = getMockForPutFunction();
    }

    @Test
    public void testGetExistingPropertyWhenPlugin() throws Exception {
        testGetExistingProperty(addonKey);
    }

    @Test
    public void testGetExistingPropertyWhenSysAdmin() throws Exception {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testGetExistingProperty(null);
    }

    @Test
    public void testGetNonExistingPropertyWhenPlugin() throws Exception {
        testGetNonExistingProperty(addonKey);
    }

    @Test
    public void testGetNonExistingPropertyWhenSysAdmin() throws Exception {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testGetNonExistingProperty(null);
    }

    @Test
    public void testPutNonExistingValidPropertyWhenPlugin() throws Exception {
        testPutNonExistingValidProperty(addonKey);
    }

    @Test
    public void testPutNonExistingValidPropertyWhenSysAdmin() throws Exception {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testPutNonExistingValidProperty(null);
    }

    @Test
    public void testPutExistingValidPropertyWhenPlugin() throws Exception {
        testPutExistingValidProperty(addonKey);
    }

    @Test
    public void testPutExistingValidPropertyWhenSysAdmin() throws Exception {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testPutExistingValidProperty(null);
    }

    @Test
    public void testDeleteExistingPropertyWhenPlugin() throws Exception {
        testDeleteExistingProperty(addonKey);
    }

    @Test
    public void testDeleteExistingPropertyWhenSysAdmin() throws Exception {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testDeleteExistingProperty(null);
    }

    @Test
    public void testDeleteNonExistingPropertyWhenPlugin() throws Exception {
        testDeleteNonExistingProperty(addonKey);
    }

    @Test
    public void testDeleteNonExistingPropertyWhenSysAdmin() throws Exception {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testDeleteNonExistingProperty(null);
    }

    @Test
    public void testListPropertiesWhenPlugin() throws Exception {
        testListProperties(addonKey);
    }

    @Test
    public void testListPropertiesWhenSysAdmin() throws Exception {
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);
        testListProperties(null);
    }

    @Test
    public void testGetInvalidPropertyWithTooLongKey() throws Exception {
        String tooLongKey = StringUtils.repeat(".", AddonPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH + 1);

        AddonPropertyService.GetServiceResult result = service.getPropertyValue(user, addonKey, addonKey, tooLongKey);
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.KEY_TOO_LONG);
    }

    @Test
    public void testPutInvalidPropertyWithTooLongKey() throws Exception {
        String tooLongKey = StringUtils.repeat(".", AddonPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH + 1);

        PutServiceResult<Void> result = service.setPropertyValueIfConditionSatisfied(user, addonKey, addonKey, tooLongKey, property.getValue().toString(), alwaysTrue());
        result.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.KEY_TOO_LONG);
    }

    @Test
    public void testDeleteInvalidPropertyWithTooLongKey() throws Exception {
        String tooLongKey = StringUtils.repeat(".", AddonPropertyAO.MAXIMUM_PROPERTY_KEY_LENGTH + 1);

        DeleteServiceResult<Void> result = service.deletePropertyValueIfConditionSatisfied(user, addonKey, addonKey, tooLongKey, alwaysTrue());
        result.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.KEY_TOO_LONG);
    }

    @Test
    public void testMaximumPropertiesExceeded() throws Exception {
        mockExecuteInTransaction();
        when(store.getPropertyValue(addonKey, property.getKey())).thenReturn(Optional.of(property));
        PutResultWithOptionalProperty mockedResult = new PutResultWithOptionalProperty(AddonPropertyStore.PutResult.PROPERTY_LIMIT_EXCEEDED, Optional.empty());
        when(store.setPropertyValue(addonKey, property.getKey(), property.getValue().toString())).thenReturn(mockedResult);

        PutServiceResult<Void> result = service.setPropertyValueIfConditionSatisfied(user, addonKey, addonKey, property.getKey(), property.getValue().toString(), alwaysTrue());
        result.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.MAXIMUM_PROPERTIES_EXCEEDED);
    }

    @Test
    public void testNoAccessToGetDifferentPluginData() throws Exception {
        AddonPropertyService.GetServiceResult result = service.getPropertyValue(user, "DIFF_PLUGIN_KEY", addonKey, property.getKey());
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
    }

    @Test
    public void testNoAccessToPutDifferentPluginData() throws Exception {
        PutServiceResult<Void> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(user, "DIFF_PLUGIN_KEY", addonKey, property.getKey(), property.getValue().toString(), alwaysTrue());
        foldableServiceResult.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
    }

    @Test
    public void testNoAccessToDeleteDifferentPluginData() throws Exception {
        DeleteServiceResult<Void> foldableServiceResult = service.deletePropertyValueIfConditionSatisfied(user, "DIFF_PLUGIN_KEY", addonKey, property.getKey(), alwaysTrue());
        foldableServiceResult.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
    }

    @Test
    public void testNoAccessToListDifferentPluginData() throws Exception {
        AddonPropertyService.GetAllServiceResult result = service.getAddonProperties(user, "DIFF_PLUGIN_KEY", addonKey);
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
    }

    @Test
    public void testGetNoAccessWhenNotLoggedIn() throws Exception {
        AddonPropertyService.GetServiceResult result = service.getPropertyValue(null, "DIFF_PLUGIN_KEY", addonKey, property.getKey());
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.NOT_AUTHENTICATED);
    }

    @Test
    public void testSetNoAccessWhenNotLoggedIn() throws Exception {
        PutServiceResult<Void> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(null, "DIFF_PLUGIN_KEY", addonKey, property.getKey(), property.getValue().toString(), alwaysTrue());
        foldableServiceResult.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.NOT_AUTHENTICATED);
    }

    @Test
    public void testDeleteNoAccessWhenLoggedIn() throws Exception {
        DeleteServiceResult<Void> foldableServiceResult = service.deletePropertyValueIfConditionSatisfied(null, "DIFF_PLUGIN_KEY", addonKey, property.getKey(), alwaysTrue());
        foldableServiceResult.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.NOT_AUTHENTICATED);
    }

    @Test
    public void testListNoAccessWhenLoggedIn() throws Exception {
        AddonPropertyService.GetAllServiceResult result = service.getAddonProperties(null, "DIFF_PLUGIN_KEY", addonKey);
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.NOT_AUTHENTICATED);
    }

    @Test
    public void testAddonNotFoundWhenPluginNotInstalledAndSysAdmin() throws Exception {
        when(connectAddonRegistry.hasAddonWithKey(addonKey)).thenReturn(false);
        when(userManager.isSystemAdmin(userKey)).thenReturn(true);

        AddonPropertyService.GetServiceResult result = service.getPropertyValue(user, null, addonKey, "");
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.ADD_ON_NOT_FOUND_OR_ACCESS_TO_OTHER_DATA_FORBIDDEN);
    }

    @Test
    public void testInvalidJson() {
        mockExecuteInTransaction();
        assertInvalidJson("[");
        assertInvalidJson("{");
    }

    @Test
    public void testInvalidJsonString() {
        assertInvalidJson("asdadf");
        assertInvalidJson("\"asdadf");
        assertInvalidJson("asdadf\"");
    }

    @Test
    public void testValidNullPrimitiveValue() throws Exception {
        mockExecuteInTransaction();
        assertValidJson("null");
    }

    @Test
    public void testValidBooleanValues() throws Exception {
        mockExecuteInTransaction();
        assertValidJson("true");
        assertValidJson("false");
    }

    @Test
    public void testValidNumbers() throws Exception {
        mockExecuteInTransaction();
        assertValidJson("0");
        assertValidJson("0.1");
        assertValidJson("2.0E5");
        assertValidJson("-4");
    }

    @Test
    public void testValidStrings() throws Exception {
        mockExecuteInTransaction();
        assertValidJson("\"asdasd\"");
    }

    @Test
    public void testValidArray() throws Exception {
        mockExecuteInTransaction();
        assertValidJson("[]");
        assertValidJson("[true]");
        assertValidJson("[true,false]");
        assertValidJson("[true, { \"hello\": 3 }]");
    }

    @Test
    public void testValidJsonValue() throws Exception {
        mockExecuteInTransaction();
        assertValidJson("{}");
        assertValidJson("{ \"k\" : true}");
    }

    @Test
    public void testPreconditionFailedCalledWhenTestFunctionReturnedFalse() {
        mockExecuteInTransaction();
        final Object obj = new Object();
        PutServiceResult<Object> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(user, addonKey, addonKey, property.getKey(), "0",
                input -> AddonPropertyService.ServiceConditionResult.FAILURE_WITH_OBJECT(obj));
        foldableServiceResult.fold(objectObjectFunction, null, null);
        verify(objectObjectFunction).apply(obj);
    }

    private void testGetExistingProperty(String sourcePlugin) {
        when(store.getPropertyValue(addonKey, property.getKey())).thenReturn(Optional.of(property));
        AddonPropertyService.GetServiceResult result = service.getPropertyValue(user, sourcePlugin, addonKey, property.getKey());
        result.fold(null, addonPropertyVoidFunction);
        verify(addonPropertyVoidFunction).apply(property);
    }

    private void testGetNonExistingProperty(String sourcePlugin) {
        when(store.getPropertyValue(addonKey, property.getKey())).thenReturn(Optional.empty());
        AddonPropertyService.GetServiceResult result = service.getPropertyValue(user, sourcePlugin, addonKey, property.getKey());
        result.fold(mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.PROPERTY_NOT_FOUND);
    }

    private void testPutNonExistingValidProperty(final String sourcePluginKey) {
        mockExecuteInTransaction();
        when(store.getPropertyValue(addonKey, property.getKey())).thenReturn(Optional.empty());

        PutResultWithOptionalProperty mockedResult = new PutResultWithOptionalProperty(AddonPropertyStore.PutResult.PROPERTY_CREATED, Optional.of(property));
        when(store.setPropertyValue(addonKey, property.getKey(), property.getValue().toString())).thenReturn(mockedResult);

        PutServiceResult<Void> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(user, sourcePluginKey, addonKey, property.getKey(), property.getValue().toString(), alwaysTrue());

        foldableServiceResult.fold(null, null, mockPutFunction);

        verify(mockPutFunction).apply(argThat(hasServiceResult(OperationStatusImpl.PROPERTY_CREATED)));
    }

    private void testPutExistingValidProperty(final String sourcePluginKey) {
        mockExecuteInTransaction();
        when(store.getPropertyValue(addonKey, property.getKey())).thenReturn(Optional.of(property));
        PutResultWithOptionalProperty mockedResult = new PutResultWithOptionalProperty(AddonPropertyStore.PutResult.PROPERTY_UPDATED, Optional.of(property));
        when(store.setPropertyValue(addonKey, property.getKey(), property.getValue().toString())).thenReturn(mockedResult);

        final PutServiceResult<Void> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(user, sourcePluginKey, addonKey, property.getKey(), property.getValue().toString(), alwaysTrue());

        foldableServiceResult.fold(null, null, mockPutFunction);
        verify(mockPutFunction).apply(argThat(hasServiceResult(OperationStatusImpl.PROPERTY_UPDATED)));
    }

    private void testDeleteExistingProperty(final String sourcePluginKey) {
        mockExecuteInTransaction();
        when(store.getPropertyValue(addonKey, property.getKey())).thenReturn(Optional.of(new AddonProperty("", EMPTY_OBJECT, 0)));

        DeleteServiceResult<Void> foldableServiceResult = service.deletePropertyValueIfConditionSatisfied(user, sourcePluginKey, addonKey, property.getKey(), alwaysTrue());
        foldableServiceResult.fold(null, null, mockFunction);
        verify(mockFunction).apply(OperationStatusImpl.PROPERTY_DELETED);
    }

    private void testDeleteNonExistingProperty(final String sourcePluginKey) {
        mockExecuteInTransaction();
        when(store.getPropertyValue(addonKey, property.getKey())).thenReturn(Optional.empty());

        DeleteServiceResult<Void> deleteServiceResult = service.deletePropertyValueIfConditionSatisfied(user, sourcePluginKey, addonKey, property.getKey(), alwaysTrue());
        deleteServiceResult.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.PROPERTY_NOT_FOUND);
    }

    private void testListProperties(final String sourcePluginKey) {
        AddonPropertyIterable emptyIterable = new AddonPropertyIterable(Collections.<AddonProperty>emptyList());
        when(store.getAllPropertiesForAddonKey(addonKey)).thenReturn(emptyIterable);

        AddonPropertyService.GetAllServiceResult result = service.getAddonProperties(user, sourcePluginKey, addonKey);
        result.fold(null, addonPropertyIterableVoidFunction);
        verify(addonPropertyIterableVoidFunction).apply(emptyIterable);
    }

    private void assertValidJson(String value) {
        PutResultWithOptionalProperty mockedResult = new PutResultWithOptionalProperty(AddonPropertyStore.PutResult.PROPERTY_UPDATED, Optional.of(
                property));
        when(store.setPropertyValue(addonKey, property.getKey(), value)).thenReturn(mockedResult);
        when(store.getPropertyValue(addonKey, property.getKey())).thenReturn(Optional.of(new AddonProperty(
                property.getKey(), property.getValue(), 0)));
        PutServiceResult<Void> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(user, addonKey, addonKey, property.getKey(), value, alwaysTrue());

        Function<AddonPropertyService.PutOperationStatus, Void> mockPutFunction = getMockForPutFunction();
        foldableServiceResult.fold(null, null, mockPutFunction);
        verify(mockPutFunction).apply(argThat(hasServiceResult(OperationStatusImpl.PROPERTY_UPDATED)));
    }

    private void assertInvalidJson(final String value) {
        PutServiceResult<Void> foldableServiceResult = service.setPropertyValueIfConditionSatisfied(user, addonKey, addonKey, property.getKey(), value, alwaysTrue());
        Function<OperationStatus, Void> mockFunction = getMockForFunction();
        foldableServiceResult.fold(null, mockFunction, null);
        verify(mockFunction).apply(OperationStatusImpl.INVALID_PROPERTY_VALUE);
    }

    private Function<Optional<AddonProperty>, AddonPropertyService.ServiceConditionResult<Void>> alwaysTrue() {
        return input -> AddonPropertyService.ServiceConditionResult.SUCCESS();
    }

    @SuppressWarnings("unchecked")
    private void mockExecuteInTransaction() {
        when(store.executeInTransaction(any(AddonPropertyStore.TransactionAction.class))).thenAnswer(
                invocationOnMock -> {
                    Object[] arguments = invocationOnMock.getArguments();
                    AddonPropertyStore.TransactionAction c = (AddonPropertyStore.TransactionAction) arguments[0];
                    return c.call();
                });
    }

    @SuppressWarnings("unchecked")
    private Function<OperationStatus, Void> getMockForFunction() {
        return (Function<OperationStatus, Void>) mock(Function.class);
    }

    @SuppressWarnings("unchecked")
    private Function<AddonPropertyService.PutOperationStatus, Void> getMockForPutFunction() {
        return (Function<AddonPropertyService.PutOperationStatus, Void>) mock(Function.class);
    }

    private ArgumentMatcher<AddonPropertyService.PutOperationStatus> hasServiceResult(final OperationStatus result) {
        return new ArgumentMatcher<AddonPropertyService.PutOperationStatus>() {
            @Override
            public boolean matches(final Object o) {
                if (o instanceof AddonPropertyService.PutOperationStatus) {
                    AddonPropertyService.PutOperationStatus other = (AddonPropertyService.PutOperationStatus) o;
                    return result == other.getBase();
                }
                return false;
            }
        };
    }
}
