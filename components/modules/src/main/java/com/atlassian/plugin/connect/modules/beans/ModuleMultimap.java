package com.atlassian.plugin.connect.modules.beans;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The modules of an add-on. Modules are loaded lazily and any validation errors are deferred to the first usage.
 * For this reason, this class provides methods for retrieving only module lists that are free of validation errors.
 */
public class ModuleMultimap
{

    private Map<String, Supplier<List<ModuleBean>>> moduleListSuppliers;

    private Map<String, List<ModuleBean>> validModules = Maps.newConcurrentMap();

    /**
     * Creates a new module multi-map.
     *
     * @param moduleListSuppliers the map of module list suppliers
     */
    public ModuleMultimap(Map<String, Supplier<List<ModuleBean>>> moduleListSuppliers)
    {
        this.moduleListSuppliers = moduleListSuppliers;
    }

    /**
     * Returns a map of module types to list of modules, excluding module types where retrieval failed, typically due
     * to validation errors.
     *
     * @param exceptionHandler the exception handler to invoke if an error occurred
     * @return the valid module lists
     */
    public Map<String, List<ModuleBean>> getValidModuleLists(Consumer<Exception> exceptionHandler)
    {
        Optional<Consumer<Exception>> optionalExceptionHandler = Optional.of(exceptionHandler);
        for (String moduleType : moduleListSuppliers.keySet())
        {
            loadExistingModuleList(moduleType, optionalExceptionHandler);
        }
        return Collections.unmodifiableMap(validModules);
    }

    /**
     * Returns a list of modules of the given type, unless a validation error occurred when retrieving it.
     *
     * @param type the module type for which to return the list of modules
     * @param exceptionHandler the exception handler to invoke if an error occurred
     * @return a list of modules, or {@link Optional#empty()} if an error occurred
     */
    public Optional<List<ModuleBean>> getValidModuleListOfType(String type, Consumer<Exception> exceptionHandler)
    {
        Optional<List<ModuleBean>> optionalModuleList = Optional.empty();
        if (moduleListSuppliers.containsKey(type))
        {
            return Optional.ofNullable(loadExistingModuleList(type, Optional.of(exceptionHandler)));
        }
        return optionalModuleList;
    }

    public <T extends ModuleBean> Optional<List<T>>  getValidModuleListOfType(Class<T> moduleClazz, Consumer<Exception> exceptionHandler)
    {
        List<ModuleBean> moduleBeanStream = getValidModuleLists(exceptionHandler).values().stream()
                                                    .flatMap(Collection::stream)
                                                    .filter(moduleBean -> moduleBean.getClass().isAssignableFrom(moduleClazz))
                                                    .collect(Collectors.toList());
        return Optional.of((List<T>) moduleBeanStream);
    }

    private List<ModuleBean> loadExistingModuleList(String moduleType, Optional<Consumer<Exception>> optionalExceptionHandler)
    {
        try
        {
            return validModules.computeIfAbsent(moduleType, (type) -> moduleListSuppliers.get(type).get());
        }
        catch (Exception e)
        {
            optionalExceptionHandler.ifPresent((exceptionConsumer) -> exceptionConsumer.accept(e));
            return null;
        }
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (!(otherObj instanceof ModuleMultimap))
        {
            return false;
        }
        ModuleMultimap other = (ModuleMultimap) otherObj;

        Consumer<Exception> noopExceptionHandler = e -> {};
        return new EqualsBuilder()
                .append(moduleListSuppliers.keySet(), other.moduleListSuppliers.keySet())
                .append(getValidModuleLists(noopExceptionHandler), other.getValidModuleLists(noopExceptionHandler))
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(41, 7)
                .append(moduleListSuppliers.keySet())
                .append(getValidModuleLists(e -> {}))
                .build();
    }
}
