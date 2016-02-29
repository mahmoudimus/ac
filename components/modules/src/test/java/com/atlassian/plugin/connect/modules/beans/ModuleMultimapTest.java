package com.atlassian.plugin.connect.modules.beans;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptySet;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ModuleMultimapTest {

    private static final String MODULE_TYPE = "testModules";
    private static final String OTHER_MODULE_TYPE = "otherTestModules";

    @Mock
    private ModuleBean moduleBeanMock;

    @Mock
    private ModuleBean otherModuleBeanMock;

    @Mock
    private RuntimeException exceptionMock;

    @Mock
    private RuntimeException otherExceptionMock;

    private Supplier<List<ModuleBean>> validSupplier = () -> Collections.singletonList(moduleBeanMock);
    private Supplier<List<ModuleBean>> otherValidSupplier = () -> Collections.singletonList(otherModuleBeanMock);

    private Supplier<List<ModuleBean>> exceptionSupplier = () -> {
        throw exceptionMock;
    };
    private Supplier<List<ModuleBean>> otherExceptionSupplier = () -> {
        throw otherExceptionMock;
    };

    @Mock
    private Consumer<Exception> exceptionHandlerMock;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldReturnAllValidModuleLists() {
        ModuleMultimap moduleMultimap = new ModuleMultimap(map(twoValidModuleListSuppliers()));
        assertModuleListEquals(twoModuleLists(), moduleMultimap.getValidModuleLists(exceptionHandlerMock));
        verifyNoMoreInteractions(exceptionHandlerMock);
    }

    @Test
    public void shouldReturnValidModuleListsAndInvokeExceptionHandlerForSkippedModuleList() {
        ModuleMultimap moduleMultimap = new ModuleMultimap(map(validAndInvalidModuleListSuppliers()));
        assertModuleListEquals(oneModuleList(), moduleMultimap.getValidModuleLists(exceptionHandlerMock));
        verify(exceptionHandlerMock).accept(exceptionMock);
        verifyNoMoreInteractions(exceptionMock);
    }

    @Test
    public void shouldInvokeExceptionHandlerForAllSkippedModuleLists() {
        ModuleMultimap moduleMultimap = new ModuleMultimap(map(twoInvalidModuleListSuppliers()));
        assertModuleListEquals(emptySet(), moduleMultimap.getValidModuleLists(exceptionHandlerMock));
        verify(exceptionHandlerMock).accept(exceptionMock);
        verify(exceptionHandlerMock).accept(otherExceptionMock);
        verifyNoMoreInteractions(exceptionMock);
    }

    @Test
    public void shouldReturnNothingForMissingModuleList() {
        ModuleMultimap moduleMultimap = new ModuleMultimap(map(twoValidModuleListSuppliers()));
        Optional<List<ModuleBean>> optionalModuleList = moduleMultimap.getValidModuleListOfType("missingModules", exceptionHandlerMock);
        assertThat(optionalModuleList.isPresent(), is(false));
        verifyNoMoreInteractions(exceptionHandlerMock);
    }

    @Test
    public void shouldReturnSingleValidModuleList() {
        ModuleMultimap moduleMultimap = new ModuleMultimap(map(twoValidModuleListSuppliers()));
        Optional<List<ModuleBean>> optionalModuleList = moduleMultimap.getValidModuleListOfType(MODULE_TYPE, exceptionHandlerMock);
        assertThat(optionalModuleList.get(), contains(moduleBeanMock));
        verifyNoMoreInteractions(exceptionHandlerMock);
    }

    @Test
    public void shouldInvokeExceptionHandlerForSkippedModuleList() {
        ModuleMultimap moduleMultimap = new ModuleMultimap(map(twoInvalidModuleListSuppliers()));
        Optional<List<ModuleBean>> optionalModuleList = moduleMultimap.getValidModuleListOfType(MODULE_TYPE, exceptionHandlerMock);
        assertThat(optionalModuleList.isPresent(), is(false));
        verify(exceptionHandlerMock).accept(exceptionMock);
        verifyNoMoreInteractions(exceptionHandlerMock);
    }

    @Test
    public void shouldTwoMultimapsWithEqualModuleListSuppliersBeEqual() {
        ModuleMultimap moduleMultimap = new ModuleMultimap(map(twoValidModuleListSuppliers()));
        ModuleMultimap otherModuleMultimap = new ModuleMultimap(map(twoValidModuleListSuppliers()));
        assertThat(moduleMultimap, equalTo(otherModuleMultimap));
        assertThat(moduleMultimap.hashCode(), equalTo(otherModuleMultimap.hashCode()));
    }

    @Test
    public void shouldTwoMultimapsWithEqualModuleListSuppliersHaveSameHashCode() {
        ModuleMultimap moduleMultimap = new ModuleMultimap(map(twoValidModuleListSuppliers()));
        ModuleMultimap otherModuleMultimap = new ModuleMultimap(map(twoValidModuleListSuppliers()));
        assertThat(moduleMultimap, equalTo(otherModuleMultimap));
        assertThat(moduleMultimap.hashCode(), equalTo(otherModuleMultimap.hashCode()));
    }

    @Test
    public void shouldTwoMultimapsWithNonEqualModuleListSuppliersBeNonEqual() {
        ModuleMultimap moduleMultimap = new ModuleMultimap(map(twoValidModuleListSuppliers()));
        ModuleMultimap otherModuleMultimap = new ModuleMultimap(map(validAndInvalidModuleListSuppliers()));
        assertThat(moduleMultimap, not(equalTo(otherModuleMultimap)));
    }

    @SuppressWarnings("unchecked")
    private HashSet<Map.Entry<String, Supplier<List<ModuleBean>>>> twoValidModuleListSuppliers() {
        return Sets.newHashSet(entry(MODULE_TYPE, validSupplier), entry(OTHER_MODULE_TYPE, otherValidSupplier));
    }

    @SuppressWarnings("unchecked")
    private HashSet<Map.Entry<String, Supplier<List<ModuleBean>>>> validAndInvalidModuleListSuppliers() {
        return Sets.newHashSet(entry(MODULE_TYPE, validSupplier), entry(OTHER_MODULE_TYPE, exceptionSupplier));
    }

    @SuppressWarnings("unchecked")
    private HashSet<Map.Entry<String, Supplier<List<ModuleBean>>>> twoInvalidModuleListSuppliers() {
        return Sets.newHashSet(entry(MODULE_TYPE, exceptionSupplier), entry(OTHER_MODULE_TYPE, otherExceptionSupplier));
    }

    @SuppressWarnings("unchecked")
    private HashSet<Map.Entry<String, List<ModuleBean>>> twoModuleLists() {
        return Sets.newHashSet(entry(MODULE_TYPE, newArrayList(moduleBeanMock)), entry(OTHER_MODULE_TYPE, newArrayList(otherModuleBeanMock)));
    }

    @SuppressWarnings("unchecked")
    private HashSet<Map.Entry<String, List<ModuleBean>>> oneModuleList() {
        return Sets.newHashSet(entry(MODULE_TYPE, newArrayList(moduleBeanMock)));
    }

    private <K, V> Map<K, V> map(Set<Map.Entry<K, V>> entries) {
        return entries.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    private void assertModuleListEquals(Set<Map.Entry<String, List<ModuleBean>>> expectedModuleLists, Map<String, List<ModuleBean>> validModuleLists) {
        assertThat(validModuleLists.keySet(), equalTo(map(expectedModuleLists).keySet()));
        for (Map.Entry<String, List<ModuleBean>> expectedModuleList : expectedModuleLists) {
            assertThat(validModuleLists, hasEntry(equalTo(expectedModuleList.getKey()), equalTo(expectedModuleList.getValue())));
        }
    }
}
