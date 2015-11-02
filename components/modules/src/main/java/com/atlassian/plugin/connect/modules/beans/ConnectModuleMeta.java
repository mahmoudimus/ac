package com.atlassian.plugin.connect.modules.beans;

/**
 * A holder of metadata for an Atlassian Connect feature module.
 *
 * @param <T> the type of the add-on descriptor module representation
 */
public abstract class ConnectModuleMeta<T extends ModuleBean>
{

    private String descriptorKey;

    private Class<T> beanClass;

    /**
     * Creates a new module meta.
     *
     * @param descriptorKey the descriptor key, must be unique
     * @param beanClass the module representation class
     */
    public ConnectModuleMeta(String descriptorKey, Class<T> beanClass)
    {
        this.descriptorKey = descriptorKey;
        this.beanClass = beanClass;
    }

    /**
     * Returns the key of the module in the associative array (JSON object) of modules.
     *
     * @return the descriptor key, must be unique
     */
    public String getDescriptorKey()
    {
        return descriptorKey;
    }

    /**
     * The class that represents the add-on descriptor module.
     *
     * @return the module representation class
     */
    public Class<T> getBeanClass()
    {
        return beanClass;
    }

    /**
     * Returns whether the entry for the module must contain an array of JSON objects. If false, a single JSON object
     * is expected.
     *
     * @return whether multiple modules are allowed, by default true
     */
    public boolean multipleModulesAllowed()
    {
        return true;
    }
}
