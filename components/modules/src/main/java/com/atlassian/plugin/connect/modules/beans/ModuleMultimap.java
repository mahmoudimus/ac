package com.atlassian.plugin.connect.modules.beans;

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
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
     * Returns a map of module types to list of modules, excluding module types that produce validation errors.
     *
     * @return the valid module lists
     */
    public Map<String, List<ModuleBean>> getValidModuleLists()
    {
        for (String moduleType : moduleListSuppliers.keySet())
        {
            loadValidModuleList(moduleType);
        }
        return Collections.unmodifiableMap(validModules);
    }

    /**
     * Returns a map of module types to list of modules.
     *
     * @return the valid module lists
     * @throws ConnectModuleValidationException if a validation error occurs for any module type
     */
    public Map<String, List<ModuleBean>> getModuleLists() throws ConnectModuleValidationException
    {
        return rethrowModuleValidationCause(new Supplier<Map<String, List<ModuleBean>>>()
        {

            @Override
            public Map<String, List<ModuleBean>> get()
            {
                return moduleListSuppliers.entrySet().stream()
                        .map(new Function<Entry<String, Supplier<List<ModuleBean>>>, Entry<String, List<ModuleBean>>>()
                        {

                            @Override
                            public Entry<String, List<ModuleBean>> apply(Entry<String, Supplier<List<ModuleBean>>> entry)
                            {
                                return new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().get());
                            }
                        })
                        .collect(Collectors.toMap(new Function<Entry<String, List<ModuleBean>>, String>()
                        {

                            @Override
                            public String apply(Entry<String, List<ModuleBean>> entry)
                            {
                                return entry.getKey();
                            }
                        }, new Function<Entry<String, List<ModuleBean>>, List<ModuleBean>>()
                        {

                            @Override
                            public List<ModuleBean> apply(Entry<String, List<ModuleBean>> entry)
                            {
                                return entry.getValue();
                            }
                        }));
            }
        });
    }

    /**
     * Returns a list of modules of the given type, unless a validation error occurred when retrieving it.
     *
     * @return a list of modules, or {@link Optional#empty()} if a validation error occurred
     */
    public Optional<List<ModuleBean>> getValidModuleListOfType(String type)
    {
        return Optional.ofNullable(loadValidModuleList(type));
    }

    /**
     * Returns a list of modules of the given type.
     *
     * @return a list of modules
     * @throws ConnectModuleValidationException if a validation error occurs
     */
    public List<ModuleBean> getModuleListOfType(String type) throws ConnectModuleValidationException
    {
        return rethrowModuleValidationCause(new Supplier<List<ModuleBean>>()
        {

            @Override
            public List<ModuleBean> get()
            {
                return moduleListSuppliers.get(type).get();
            }
        });
    }

    private <T> T rethrowModuleValidationCause(Supplier<T> supplier) throws ConnectModuleValidationException
    {
        try
        {
            return supplier.get();
        } catch (RuntimeException e)
        {
            Throwables.propagateIfInstanceOf(e.getCause(), ConnectModuleValidationException.class);
            throw e;
        }
    }

    private List<ModuleBean> loadValidModuleList(String moduleType)
    {
        try
        {
            return validModules.computeIfAbsent(moduleType, new Function<String, List<ModuleBean>>()
            {

                @Override
                public List<ModuleBean> apply(String type)
                {
                    return moduleListSuppliers.get(type).get();
                }
            });
        } catch (Exception e)
        {
            // Skip
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

        return new EqualsBuilder()
                .append(moduleListSuppliers.keySet(), other.moduleListSuppliers.keySet())
                .append(getValidModuleLists(), other.getValidModuleLists())
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(41, 7)
                .append(moduleListSuppliers.keySet())
                .append(getValidModuleLists())
                .build();
    }
}
